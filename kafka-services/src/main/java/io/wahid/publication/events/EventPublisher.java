package io.wahid.publication.events;

import io.wahid.publication.events.event.DomainEvent;
import org.apache.avro.specific.SpecificRecordBase;

public interface EventPublisher {
    void publish(String topic, DomainEvent event);
    void publish(String topic, SpecificRecordBase event);
    void flush();
    void close();
}
