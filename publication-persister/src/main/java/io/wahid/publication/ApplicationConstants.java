package io.wahid.publication;

public class ApplicationConstants {
    public static final String BOOTSTRAP_SERVER = System.getenv().getOrDefault("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092");

    public static final int BATCH_SIZE = 1000;
    public static final int DB_BATCH_SIZE = 10000;

    public static final String BOOK_TOPIC = "books";
    public static final String MAGAZINE_TOPIC = "magazines";
    public static final String AUTHOR_TOPIC = "authors";

    public static final String PUBLICATION_READY_TOPIC = "publication-ready";

    public static final String AUTHOR_DB_WRITER_GROUP_ID = "author-db-writer-group";
    public static final String PENDING_PUBLICATION_CREATED_TOPIC = "pending-publication-created";
    public static final String PUBLICATION_DB_WRITER_GROUP_ID = "publication-db-writer-group";
    public static final String PENDING_PUBLICATION_DB_WRITER_GROUP_ID = "pending_publication-db-writer-group";
    public static final String PUBLICATION_DB_WRITER_CONSUMER_GROUP_ID = "publication-db-writer-consumer-group";

    public static final String AUTHOR_DB_WRITER_TOPIC = "author-db-writer";

    public static final String AUTHOR_CREATED_TOPIC = "author-created";
    public static final String PUBLICATION_CREATED_TOPIC = "publication-created";
    public static final String PUBLICATION_ORCHESTRATOR_GROUP_ID = "publication-orchestrator-group-v2";

    public static final String NO_PENDING = "no-pending";
    public static final String PROCESS_COMPLETE = "process-complete";

    public static final String EVENT_KEY_SERIALIZER = "org.apache.kafka.common.serialization.StringSerializer";
    public static final String EVENT_VALUE_SERIALIZER = "org.apache.kafka.common.serialization.StringSerializer";
    public static final String EVENT_KEY_DESERIALIZER = "org.apache.kafka.common.serialization.StringDeserializer";
    public static final String EVENT_VALUE_DESERIALIZER = "org.apache.kafka.common.serialization.StringDeserializer";

}
