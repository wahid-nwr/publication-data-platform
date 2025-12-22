package io.wahid.publication.events.event;

import io.wahid.publication.model.Author;

import java.time.Instant;

public class AuthorReadyEvent implements DomainEvent {
    private final Instant occurredAt = Instant.now();
    private final String eventType = "AuthorPersisted";
    private final Author author;

    public AuthorReadyEvent(Author author) {
        super();
        this.author = author;
    }

    public AuthorReadyEvent() {
        super();
        this.author = null;
    }

    @Override
    public String getEventType() {
        return this.eventType;
    }

    @Override
    public Instant getOccurredAt() {
        return this.occurredAt;
    }

    @Override
    public String toString() {
        return "AuthorReadyEvent{" +
                "occurredAt=" + occurredAt +
                ", eventType='" + eventType + '\'' +
                ", author=" + author +
                '}';
    }

    public Author getAuthor() {
        return this.author;
    }
}
