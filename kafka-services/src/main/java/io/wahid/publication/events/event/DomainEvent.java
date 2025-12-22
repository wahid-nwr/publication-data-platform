package io.wahid.publication.events.event;

import java.time.Instant;

public interface DomainEvent {
    String getEventType();
    Instant getOccurredAt();
}
