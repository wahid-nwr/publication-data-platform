package io.wahid.publication.service.kafka.dbwriter;

import io.wahid.publication.events.EventPublisher;
import io.wahid.publication.events.KafkaEventPublisher;
import io.wahid.publication.model.events.PendingPublication;
import io.wahid.publication.model.events.Status;
import io.wahid.publication.model.events.TYPE;
import io.wahid.publication.repository.events.PendingPublicationRepository;
import io.wahid.publication.util.JpaUtil;
import io.wahid.publication.util.KafkaUtil;
import com.publication.events.PublicationCreatedEvent;
import com.publication.events.PublicationReadyEvent;
import io.wahid.publication.KafkaConstants;
import jakarta.persistence.EntityManager;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.TopicPartition;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PendingPublicationDBWriter {
    private static final Logger LOGGER = Logger.getLogger(PendingPublicationDBWriter.class.getName());
    private static final int PROCESS_BATCH_SIZE = 5000;
    private final PendingPublicationRepository repo;
    private final EventPublisher publisher;

    public PendingPublicationDBWriter(String bootstrapServers) {
        repo = new PendingPublicationRepository(JpaUtil.getEntityManagerFactory());

        Properties producerProps = KafkaUtil.getProducerProperties(bootstrapServers);
        KafkaProducer<String, SpecificRecordBase> producer = new KafkaProducer<>(producerProps);
        this.publisher = new KafkaEventPublisher(null, producer);
    }

    public Map<TopicPartition, OffsetAndMetadata> run(List<ConsumerRecord<String, SpecificRecordBase>> records) throws IOException {
        Set<PendingPublication> batch = new HashSet<>(PROCESS_BATCH_SIZE);
        Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();
        LOGGER.log(Level.INFO, "Pending publication db write consumer started");
        for (ConsumerRecord<String, SpecificRecordBase> consumerRecord : records) {
            byte[] avroBytes = KafkaUtil.getKafkaBytes(consumerRecord.value());
            DatumReader<PublicationReadyEvent> reader = new SpecificDatumReader<>(PublicationReadyEvent.class);
            Decoder decoder = DecoderFactory.get().binaryDecoder(avroBytes, null);
            PublicationReadyEvent event = reader.read(null, decoder);

            // If not all authors are present, skip (or retry later)
            String id = UUID.randomUUID().toString();
            List<String> emails = event.getAuthorEmails().stream().map(CharSequence::toString).toList();
            PendingPublication pub = null;
            if (TYPE.BOOK.name().contentEquals(event.getType())) {
                pub = new PendingPublication(id, event.getTitle().toString(), event.getIsbn().toString(), emails,
                        event.getDescription().toString(), Status.PENDING);
            } else if (TYPE.MAGAZINE.name().contentEquals(event.getType())) {
                List<Integer> dateValues = event.getPublicationDate();
                LocalDate date = LocalDate.of(dateValues.get(0), dateValues.get(1), dateValues.get(2));
                pub = new PendingPublication(id, event.getTitle().toString(), event.getIsbn().toString(), emails,
                        date, Status.PENDING);
            }
            batch.add(pub);

            TopicPartition partition = new TopicPartition(consumerRecord.topic(), consumerRecord.partition());
            OffsetAndMetadata metaData = new OffsetAndMetadata(consumerRecord.offset() + 1);
            currentOffsets.put(partition, metaData);
        }
        if (!batch.isEmpty()) {
            List<PublicationCreatedEvent> events = persistBatch(batch);
            events.forEach(event -> publisher.publish(KafkaConstants.PENDING_PUBLICATION_CREATED_TOPIC, event));
            publisher.flush();
            batch.clear();
        }
        return currentOffsets;
    }

    private List<PublicationCreatedEvent> persistBatch(Set<PendingPublication> batch) {
        List<PublicationCreatedEvent> events = new ArrayList<>(batch.size());
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            for (PendingPublication pending : batch) {
                LocalDate date = pending.getPublicationDate() != null ? pending.getPublicationDate() : LocalDate.now();
                List<Integer> dateValues = List.of(date.getYear(), date.getMonthValue(), date.getDayOfMonth());

                if (!repo.doesExistWithIsbn(pending.getIsbn())) {
                    repo.save(pending);
                }
                PublicationCreatedEvent event = new PublicationCreatedEvent();
                event.setEventType(pending.getType().name());
                event.setType(pending.getType().name());
                event.setPublicationId(pending.getId());
                event.setOccurredAt(Instant.now().toEpochMilli());

                com.publication.events.PendingPublication pub = new com.publication.events.PendingPublication();
                pub.setTitle(pending.getTitle());
                pub.setIsbn(pending.getIsbn());
                pub.setDescription(pending.getDescription());
                pub.setStatus(pending.getStatus().name());
                pub.setPublicationDate(dateValues);
                pub.setType(pending.getType().name());
                pub.setAuthorEmails(new ArrayList<>(pending.getAuthorEmails()));
                event.setPendingPublication(pub);
                events.add(event);
            }
            LOGGER.log(Level.INFO, "✅ Persisted pending publications batch of size: {0}", batch.size());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            em.close();
        }
        return events;
    }
}
