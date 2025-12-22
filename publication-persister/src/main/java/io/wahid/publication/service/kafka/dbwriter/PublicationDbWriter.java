package io.wahid.publication.service.kafka.dbwriter;

import io.wahid.publication.blockchain.BlockchainClient;
import io.wahid.publication.blockchain.BlockchainService;
import io.wahid.publication.model.Author;
import io.wahid.publication.model.Book;
import io.wahid.publication.model.Magazine;
import io.wahid.publication.model.PublicationModel;
import io.wahid.publication.model.events.TYPE;
import io.wahid.publication.model.web3.PublicationTransaction;
import io.wahid.publication.repository.PublicationRepository;
import io.wahid.publication.util.JpaUtil;
import io.wahid.publication.util.KafkaUtil;
import com.publication.events.PublicationReadyEvent;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.web3j.crypto.Hash;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PublicationDbWriter {

    private static final Logger LOGGER = Logger.getLogger(PublicationDbWriter.class.getName());
    private static final int PROCESS_BATCH_SIZE = 5000;

    private final EntityManagerFactory emf;
    private final PublicationRepository repo;
    private final BlockchainService blockchainService;

    public PublicationDbWriter() throws Exception {
        this.emf = JpaUtil.getEntityManagerFactory();
        this.repo = new PublicationRepository(emf);
        BlockchainClient blockchainClient = new BlockchainClient();
        this.blockchainService = new BlockchainService(blockchainClient);
    }

    public Map<TopicPartition, OffsetAndMetadata> run(List<ConsumerRecord<String, SpecificRecordBase>> records)
            throws IOException {
        Set<PublicationModel> batch = new HashSet<>(PROCESS_BATCH_SIZE);
        Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();
        for (ConsumerRecord<String, SpecificRecordBase> consumerRecord : records) {
            byte[] avroBytes = KafkaUtil.getKafkaBytes(consumerRecord.value());
            DatumReader<PublicationReadyEvent> reader = new SpecificDatumReader<>(PublicationReadyEvent.class);
            Decoder decoder = DecoderFactory.get().binaryDecoder(avroBytes, null);
            PublicationReadyEvent event = reader.read(null, decoder);
            LOGGER.log(Level.INFO, "publication to persist event -> {0}", event);
            // Fetch authors from DB
            List<String> authorEmails = event.getAuthorEmails().stream().map(CharSequence::toString).toList();
            List<Author> authors = getAuthors(authorEmails);

            // If not all authors are present, skip (or retry later)
            if (authors.size() < event.getAuthorEmails().size()) {
                LOGGER.log(Level.INFO, "⚠ Skipping publication {0} because not all authors are present", event.getPublicationId());
                continue;
            }

            PublicationModel pub = null;
            if (TYPE.BOOK.name().contentEquals(event.getType())) {
                pub = new Book(event.getTitle().toString(), event.getIsbn().toString(), authors,
                        event.getDescription().toString());
            } else if (TYPE.MAGAZINE.name().contentEquals(event.getType())) {
                List<Integer> dateValues = event.getPublicationDate();
                LocalDate date = LocalDate.of(dateValues.get(0), dateValues.get(1), dateValues.get(2));
                pub = new Magazine(event.getTitle().toString(), event.getIsbn().toString(), authors, date);
            }

            batch.add(pub);

            TopicPartition partition = new TopicPartition(consumerRecord.topic(), consumerRecord.partition());
            OffsetAndMetadata metaData = new OffsetAndMetadata(consumerRecord.offset() + 1);
            currentOffsets.put(partition, metaData);
        }
        if (!batch.isEmpty()) {
            persistBatch(batch);
            batch.clear();
        }
        return currentOffsets;
    }

    private List<Author> getAuthors(List<String> authorEmails) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT a FROM Author a WHERE a.email IN :emails", Author.class)
                    .setParameter("emails", authorEmails)
                    .getResultList();
        }
    }

    private void persistBatch(Set<PublicationModel> batch) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            for (PublicationModel publicationModel : batch) {
                if (repo.doesExistWithIsbn(publicationModel.getIsbn())) {
                    continue;
                }
                LOGGER.log(Level.INFO, "publication to persist -> {0}", publicationModel);
                em.persist(publicationModel);
                PublicationTransaction publicationTransaction = publishHash(publicationModel);
                if (publicationTransaction != null) {
                    em.persist(publicationTransaction);
                }
            }
            tx.commit();
            LOGGER.log(Level.INFO, "✅ Persisted publications batch of size: {0}", batch.size());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            tx.rollback();
        } finally {
            em.close();
        }
    }

    private PublicationTransaction publishHash(PublicationModel publication) throws Exception {
        String canonical = publication.toCanonicalString();
        byte[] hashBytes = Hash.sha3(
                canonical.getBytes(StandardCharsets.UTF_8)
        );
        if (blockchainService.isHashStored(hashBytes)){
            // If sent, will be rejected automatically, but with a failed transaction and fee deducted.
            LOGGER.log(Level.INFO, "Hash already stored!");
            return null;
        } else {
            LOGGER.log(Level.INFO, "Hash length = {0}", hashBytes.length);
            LOGGER.log(Level.INFO, "sending hash:   0x{0}", Numeric.toHexStringNoPrefix(hashBytes));
            TransactionReceipt receipt = blockchainService.storePublicationHash(publication.getIsbn(), hashBytes);
            receipt.getTransactionHash();
            LOGGER.log(Level.INFO, "receipt.getEffectiveGasPrice()->{0}", receipt.getEffectiveGasPrice());
            return new PublicationTransaction(UUID.randomUUID().toString(), publication.getIsbn(), receipt.getTransactionHash());
        }
    }
}
