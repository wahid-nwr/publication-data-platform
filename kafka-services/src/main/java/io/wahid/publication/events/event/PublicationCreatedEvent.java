package io.wahid.publication.events.event;

import io.wahid.publication.model.events.PendingPublication;
import io.wahid.publication.model.events.TYPE;

import java.time.Instant;
import java.util.UUID;

public class PublicationCreatedEvent implements DomainEvent {
    private final String publicationId;
    private final TYPE type; // BOOK, MAGAZINE
    private final PendingPublication pendingPublication;
    private final Instant occurredAt = Instant.now();
    private final String eventType = "PublicationCreated";

    public PublicationCreatedEvent() {
        super();
        this.publicationId = null;
        this.type = null;
        this.pendingPublication = null;
    }

    public PublicationCreatedEvent(String publicationId, PendingPublication pendingPublication) {
        this.publicationId = publicationId;
        this.type = pendingPublication.getType();
        this.pendingPublication = pendingPublication;
    }

    // Getters and Setters
    public String getPublicationId() { return publicationId; }
    public TYPE getType() { return type; }
    public PendingPublication getPendingPublication() { return pendingPublication; }

    @Override
    public String getEventType() { return eventType; }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }

    @Override
    public String toString() {
        return "PublicationReadyEvent{" +
                "publicationId='" + publicationId + '\'' +
                ", type=" + type +
                ", pendingPublication=" + pendingPublication +
                ", occurredAt=" + occurredAt +
                ", eventType='" + eventType + '\'' +
                '}';
    }

    public PublicationCreatedEvent getPublicationReadyEvent(PendingPublication pendingPublication) {
        return new PublicationCreatedEvent(UUID.randomUUID().toString(), pendingPublication);
    }
}
