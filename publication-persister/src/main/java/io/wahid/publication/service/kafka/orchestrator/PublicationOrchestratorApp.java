package io.wahid.publication.service.kafka.orchestrator;

import io.wahid.publication.PrintAppResult;
import io.wahid.publication.model.events.Status;
import io.wahid.publication.repository.DBSanitizer;
import io.wahid.publication.repository.PublicationRepository;
import io.wahid.publication.repository.events.PendingPublicationRepository;
import io.wahid.publication.service.kafka.dbwriter.AuthorDbWriter;
import io.wahid.publication.service.kafka.dbwriter.PendingPublicationDBWriter;
import io.wahid.publication.service.kafka.dbwriter.PublicationDbWriter;
import io.wahid.publication.util.JpaUtil;
import io.wahid.publication.util.KafkaUtil;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import io.wahid.publication.KafkaConstants;
import io.wahid.publication.observability.MetricsServer;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.StreamSupport;

public class PublicationOrchestratorApp {
    private static final Logger LOGGER = Logger.getLogger(PublicationOrchestratorApp.class.getName());
    private static final String CLIENT_ID = "publication-orchestrator-consumer";
    private final String onlyConsumeMessages = System.getenv().getOrDefault("CONSUME_ONLY", "false");
    private final KafkaConsumer<String, SpecificRecordBase> consumer;
    private PendingPublicationRepository repo;
    private PublicationRepository publicationRepository;
    private PendingPublicationDBWriter pendingPublicationWriter;
    private AuthorDbWriter authorDbWriterService;
    private PublicationDbWriter publicationDbWriterService;
    private PrintAppResult printAppResult;
    private Counter messageCounter;
    private Counter dbInsertCounter;
    private Counter errorCounter;
    private Timer processingTimer;

    private PublicationOrchestrator orchestrator;
    private boolean propEnsured = false;

    public PublicationOrchestratorApp() {
        LOGGER.log(Level.INFO, "⏳ Starting PublicationOrchestrator app");
        Properties props = KafkaUtil.gerConsumerProperties(KafkaConstants.BOOTSTRAP_SERVER, KafkaConstants.PUBLICATION_ORCHESTRATOR_GROUP_ID, CLIENT_ID);
        this.consumer = new KafkaConsumer<>(props);
        // 🔑 Subscribe to all relevant topics
        LOGGER.log(Level.INFO, "⏳ Subscribing consumers...");
        consumer.subscribe(Arrays.asList(KafkaConstants.AUTHOR_CREATED_TOPIC, KafkaConstants.PUBLICATION_CREATED_TOPIC,
                KafkaConstants.PUBLICATION_READY_TOPIC, KafkaConstants.AUTHOR_TOPIC, KafkaConstants.PENDING_PUBLICATION_CREATED_TOPIC));
    }

    public void run() throws Exception {
        LOGGER.log(Level.INFO, "🔑 PublicationOrchestrator started. Orchestrator Subscription confirmed: {0}",
                consumer.subscription());
        digestAll(Boolean.getBoolean(onlyConsumeMessages));
        while (true) {
            ConsumerRecords<String, SpecificRecordBase> records = consumer.poll(Duration.ofMillis(1000));
            LOGGER.log(Level.INFO, "Polled {0} records", records.count());

            ensureRepos();
            if (printOrSearchPending(records)) break;

            List<ConsumerRecord<String, SpecificRecordBase>> authors = processDBWrite(KafkaConstants.AUTHOR_CREATED_TOPIC, records);
            List<ConsumerRecord<String, SpecificRecordBase>> pendingPublications = processDBWrite(KafkaConstants.PUBLICATION_CREATED_TOPIC, records);
            List<ConsumerRecord<String, SpecificRecordBase>> publications = processDBWrite(KafkaConstants.PUBLICATION_READY_TOPIC, records);

            Map<TopicPartition, OffsetAndMetadata> otherOffsets = new HashMap<>();
            for (var consumerRecord : records) {
                messageCounter.increment();
                processingTimer.record(() -> {
                    try {
                        if (pendingPublications.contains(consumerRecord) || publications.contains(consumerRecord)
                                || authors.contains(consumerRecord)) {
                            return;
                        }
                        String topic = consumerRecord.topic();
                        handleEvent(consumerRecord);

                        TopicPartition partition = new TopicPartition(topic, consumerRecord.partition());
                        OffsetAndMetadata metaData = new OffsetAndMetadata(consumerRecord.offset() + 1);
                        otherOffsets.put(partition, metaData);
                        dbInsertCounter.increment();
                    } catch (Exception e) {
                        errorCounter.increment();
                        LOGGER.log(Level.WARNING, "An error occurred trying to handle consumer record.", e);
                    }
                });

            }
            consumer.commitSync(otherOffsets);
        }
    }

    private synchronized void ensureRepos() throws Exception {
        if (propEnsured) return;
        Logger.getGlobal().info("⏳ Loading repository and writers");
        var emf = JpaUtil.getEntityManagerFactory();
        Logger.getGlobal().info("⏳ Loaded entity manager factory!");
        this.repo = new PendingPublicationRepository(emf);
        this.publicationRepository = new PublicationRepository(emf);
        Logger.getGlobal().info("⏳ Loaded repository!");
        this.pendingPublicationWriter = new PendingPublicationDBWriter(KafkaConstants.BOOTSTRAP_SERVER);
        this.authorDbWriterService = new AuthorDbWriter(KafkaConstants.BOOTSTRAP_SERVER);
        this.publicationDbWriterService = new PublicationDbWriter();
        Logger.getGlobal().info("⏳ Loaded writers!");
        this.printAppResult = new PrintAppResult();
        new DBSanitizer().sanitizeDB();
        Logger.getGlobal().info("⏳ Loaded sanitizer!");

        messageCounter = MetricsServer.MetricsRegistry.REGISTRY.counter("kafka_messages_total", "type", "publication");
        dbInsertCounter = MetricsServer.MetricsRegistry.REGISTRY.counter("db_inserts_total", "table", "pending_publications");
        errorCounter = MetricsServer.MetricsRegistry.REGISTRY.counter("processing_errors_total");
        processingTimer = MetricsServer.MetricsRegistry.REGISTRY.timer("message_processing_time");
        propEnsured = true;
    }

    private void handleEvent(ConsumerRecord<String, SpecificRecordBase> consumerRecord) throws Exception {
        if (orchestrator == null) {
            orchestrator = new PublicationOrchestrator();
        }
        orchestrator.handleEvent(consumerRecord);
    }

    private List<ConsumerRecord<String, SpecificRecordBase>> processDBWrite(String topic, ConsumerRecords<String, SpecificRecordBase> records)
            throws IOException {
        Spliterator<ConsumerRecord<String, SpecificRecordBase>> spliterator = records.records(topic).spliterator();
        List<ConsumerRecord<String, SpecificRecordBase>> consumerRecords = StreamSupport.stream(spliterator, false).toList();
        Map<TopicPartition, OffsetAndMetadata> currentOffsets;
        if (!consumerRecords.isEmpty()) {
            currentOffsets = switch (topic) {
                case KafkaConstants.AUTHOR_CREATED_TOPIC -> authorDbWriterService.run(consumerRecords);
                case KafkaConstants.PUBLICATION_CREATED_TOPIC -> pendingPublicationWriter.run(consumerRecords);
                case KafkaConstants.PUBLICATION_READY_TOPIC -> publicationDbWriterService.run(consumerRecords);
                default -> new HashMap<>();
            };
            consumer.commitSync(currentOffsets);
        }
        return consumerRecords;
    }

    private boolean printOrSearchPending(ConsumerRecords<String, SpecificRecordBase> records) {
        boolean isComplete = false;
        if (records.isEmpty()) {
            if (repo.hasPending()) {
                repo.findByStatus(Status.PENDING, 0, 10).forEach(orchestrator::tryMakePublicationReady);
            }
            if (publicationRepository.count() == 14) {
                printAppResult.print();
                isComplete = true;
            }
        }
        return isComplete;
    }

    private void digestAll(boolean onlyConsume) {
        while (onlyConsume) {
            ConsumerRecords<String, SpecificRecordBase> records = consumer.poll(Duration.ofMillis(1000));
            Map<TopicPartition, OffsetAndMetadata> otherOffsetsDigest = new HashMap<>();
            for (var consumerRecord : records) {
                String topic = consumerRecord.topic();
                TopicPartition partition = new TopicPartition(topic, consumerRecord.partition());
                OffsetAndMetadata metaData = new OffsetAndMetadata(consumerRecord.offset() + 1);
                otherOffsetsDigest.put(partition, metaData);
            }
            consumer.commitSync(otherOffsetsDigest);
        }
    }
}
