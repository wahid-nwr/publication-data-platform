package io.wahid.publication.events.event;

import java.time.Instant;

public class ProcessCompleteEvent implements DomainEvent {
    private final Instant occurredAt = Instant.now();
    private final String eventType = "Completed";

    @Override
    public String getEventType() {
        return eventType;
    }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }
}
