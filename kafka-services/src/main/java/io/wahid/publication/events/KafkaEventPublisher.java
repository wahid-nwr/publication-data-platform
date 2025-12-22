package io.wahid.publication.events;

import io.wahid.publication.events.event.DomainEvent;
import io.wahid.publication.util.JsonUtil;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.UUID;

public class KafkaEventPublisher implements EventPublisher {

    private final KafkaProducer<String, String> producer;
    private final KafkaProducer<String, SpecificRecordBase> avroProducer;

    public KafkaEventPublisher(KafkaProducer<String, String> producer) {
        this.producer = producer;
        this.avroProducer = null;
    }

    public KafkaEventPublisher(KafkaProducer<String, String> producer, KafkaProducer<String, SpecificRecordBase> avroProducer) {
        this.producer = producer;
        this.avroProducer = avroProducer;
    }

    @Override
    public void publish(String topic, SpecificRecordBase event) {
        try {
            ProducerRecord<String, SpecificRecordBase> record =
                    new ProducerRecord<>(topic, UUID.randomUUID().toString(), event);

            avroProducer.send(record, (metadata, ex) -> {
                if (ex != null) {
                    System.err.println("❌ Failed to send author event: " + ex.getMessage());
                } else {
                    System.out.printf("✅ Sent event to %s [%d@%d] - $s %n",
                            metadata.topic(), metadata.partition(), metadata.offset(), event);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish event", e);
        }
    }

    @Override
    public void publish(String topic, DomainEvent event) {
        try {
            String payload = JsonUtil.toJson(event);
            ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic, UUID.randomUUID().toString(), payload);
            producer.send(producerRecord, (metadata, ex) -> {
                if (ex != null) {
                    System.err.println("Failed to send author event: " + ex.getMessage());
                }
            });
            producer.flush();
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish event", e);
        }
    }

    @Override
    public void flush() {
        if (producer != null) producer.flush();
        if (avroProducer != null) avroProducer.flush();
    }

    @Override
    public void close() {
        if (producer != null) producer.close();
        if (avroProducer != null) avroProducer.close();
    }
}