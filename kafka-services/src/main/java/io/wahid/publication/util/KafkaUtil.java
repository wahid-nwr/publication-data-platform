package io.wahid.publication.util;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

public class KafkaUtil {
    private static final String SCHEMA_REGISTRY = System.getenv().getOrDefault("KAFKA_SCHEMA_REGISTRY", "https://psrc-81qy1r.us-west1.gcp.confluent.cloud");
    private static final String SCHEMA_REGISTRY_BASIC_AUTH = "FBLNUTAI3MJ3P7KJ:cfltJbd8RDVm3dofGfvhct7S4qT8PZNeRSnY0ZqmLeiqyr4azYFMRBfntcAtm80A";
    private KafkaUtil() {}

    public static Properties getProducerProperties(String bootstrapServers) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "1");
        props.put(ProducerConfig.ACKS_CONFIG, "1");
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, "33554432");
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, "16384");
        props.put(ProducerConfig.LINGER_MS_CONFIG, "10");

        props.put("value.subject.name.strategy", "io.confluent.kafka.serializers.subject.TopicNameStrategy");
        // Schema Registry
        props.put("schema.registry.url", SCHEMA_REGISTRY);
        props.put("basic.auth.credentials.source", "USER_INFO");
        props.put("basic.auth.user.info", SCHEMA_REGISTRY_BASIC_AUTH);

        props.put("security.protocol", "SASL_SSL");
        props.put("sasl.mechanism", "PLAIN");
        props.put("ssl.endpoint.identification.algorithm", "");
//        props.put("sasl.jaas.config", System.getenv("KAFKA_SASL_JAAS_CONFIG"));
        props.put(
                "sasl.jaas.config",
                "org.apache.kafka.common.security.plain.PlainLoginModule required " +
                        "username='DRVLONONKVQ46SJK' " +
                        "password='cfltRl737pTCUGonN6hUMpR8+a5uKaVRxUnNjf8F538KPAKRZpB8TnA/QxWej6Cg';"
        );
        return props;
    }

    public static Properties gerConsumerProperties(String bootstrapServers, String groupId, String clientId) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, clientId);

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        props.put("specific.avro.reader", true);
        props.put("schema.registry.url", SCHEMA_REGISTRY);
        props.put("basic.auth.credentials.source", "USER_INFO");
        props.put("basic.auth.user.info", SCHEMA_REGISTRY_BASIC_AUTH);

        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "5000");
        props.put("security.protocol", "SASL_SSL");
        props.put("sasl.mechanism", "PLAIN");
        props.put("ssl.endpoint.identification.algorithm", "");
        props.put("sasl.jaas.config", System.getenv("KAFKA_SASL_JAAS_CONFIG"));
        return props;
    }

    public static byte[] getKafkaBytes(GenericRecord genericRecord) throws IOException {
        DatumWriter<GenericRecord> writer = new GenericDatumWriter<>(genericRecord.getSchema());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Encoder encoder = EncoderFactory.get().binaryEncoder(out, null);
        writer.write(genericRecord, encoder);
        encoder.flush();
        return out.toByteArray();
    }
}
