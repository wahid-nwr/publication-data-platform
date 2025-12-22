package io.wahid.publication.service.kafka.dbwriter;

import io.wahid.publication.events.EventPublisher;
import io.wahid.publication.events.KafkaEventPublisher;
import io.wahid.publication.model.Author;
import io.wahid.publication.repository.AuthorRepository;
import io.wahid.publication.util.JpaUtil;
import io.wahid.publication.util.KafkaUtil;
import com.publication.events.AuthorCreatedEvent;
import com.publication.events.AuthorReadyEvent;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.wahid.publication.KafkaConstants;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
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
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AuthorDbWriter {
    private static final Logger LOGGER = Logger.getLogger(AuthorDbWriter.class.getName());
    private static final int DB_BATCH_SIZE = 1000;
    private final EventPublisher publisher;
    private final AuthorRepository repo;
    private final Counter authorsPersisted;
    private final Timer persistTimer;
    private final Tracer tracer;
    MeterRegistry registry = new SimpleMeterRegistry();

    public AuthorDbWriter(String bootstrapServers) {
        this.repo = new AuthorRepository(JpaUtil.getEntityManagerFactory());

        Properties producerProps = KafkaUtil.getProducerProperties(bootstrapServers);
        KafkaProducer<String, SpecificRecordBase> producer = new KafkaProducer<>(producerProps);
        this.publisher = new KafkaEventPublisher(null, producer);

        Metrics.addRegistry(registry);
        this.authorsPersisted = Counter.builder("publication.authors.persisted")
                .description("Number of authors persisted successfully")
                .register(registry);
        this.persistTimer = Timer.builder("publication.persist.batch.time")
                .register(registry);
        this.tracer = GlobalOpenTelemetry.getTracer("publication-orchestrator");
    }

    public Map<TopicPartition, OffsetAndMetadata> run(List<ConsumerRecord<String, SpecificRecordBase>> records) throws IOException {
        Set<Author> batch = new HashSet<>(DB_BATCH_SIZE);
        Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();
        for (ConsumerRecord<String, SpecificRecordBase> consumerRecord : records) {
            byte[] avroBytes = KafkaUtil.getKafkaBytes(consumerRecord.value());
            DatumReader<AuthorCreatedEvent> reader = new SpecificDatumReader<>(AuthorCreatedEvent.class);
            Decoder decoder = DecoderFactory.get().binaryDecoder(avroBytes, null);
            AuthorCreatedEvent event = reader.read(null, decoder);

            if (repo.doesExist(event.getEmail().toString())) {
                System.out.println("duplicate author ->" + event.getEmail());
                TopicPartition partition = new TopicPartition(consumerRecord.topic(), consumerRecord.partition());
                OffsetAndMetadata metaData = new OffsetAndMetadata(consumerRecord.offset() + 1);
                currentOffsets.put(partition, metaData);
                continue;
            }
            Author author = new Author(event.getEmail().toString(), event.getFirstName().toString(), event.getLastName().toString());
            batch.add(author);

            TopicPartition partition = new TopicPartition(consumerRecord.topic(), consumerRecord.partition());
            OffsetAndMetadata metaData = new OffsetAndMetadata(consumerRecord.offset() + 1);
            currentOffsets.put(partition, metaData);
        }
        if (!batch.isEmpty()) {
            processBatch(batch);
        }
        return currentOffsets;
    }

    private void processBatch(Set<Author> batch) {
        List<AuthorReadyEvent> events = new ArrayList<>(batch.size());

        Span span = tracer.spanBuilder("processData").startSpan();
        try (Scope scope = span.makeCurrent()) {
            persistTimer.record(() -> events.addAll(persistBatch(batch)));
            authorsPersisted.increment(batch.size());
            batch.clear();
        } finally {
            span.end();
        }
        events.forEach(event -> this.publisher.publish(KafkaConstants.AUTHOR_TOPIC, event));
        this.publisher.flush();
    }

    private List<AuthorReadyEvent> persistBatch(Set<Author> batch) {
        List<AuthorReadyEvent> events = new ArrayList<>(batch.size());
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            for (Author author : batch) {
                em.persist(author);
                AuthorReadyEvent event = new AuthorReadyEvent();
                event.setOccurredAt(Instant.now().toEpochMilli());
                event.setEventType("AuthorPersisted");
                com.publication.events.Author eventsAuthor = new com.publication.events.Author();
                eventsAuthor.setEmail(author.getEmail());
                eventsAuthor.setId(author.getId());
                eventsAuthor.setFirstName(author.getFirstName());
                eventsAuthor.setLastName(author.getLastName());
                event.setAuthor(eventsAuthor);
                events.add(event);
            }
            tx.commit();
            LOGGER.log(Level.INFO, "✅ Persisted authors batch of size: {0}", batch.size());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            if (tx.isActive()) {
                tx.rollback();
            }
        } finally {
            em.close();
        }
        return events;
    }
}