package io.wahid.publication.events.event;

import io.wahid.publication.dto.AuthorDto;

import java.time.Instant;
import java.util.UUID;

public class AuthorCreatedEvent implements DomainEvent {
    private final String authorId;
    private final String email;
    private final String firstName;
    private final String lastName;
    private final Instant occurredAt = Instant.now();
    private final String eventType = "AuthorCreated";

    public AuthorCreatedEvent() {
        super();
        this.authorId = null;
        this.email = null;
        this.firstName = null;
        this.lastName = null;
    }

    public AuthorCreatedEvent(String authorId, String email, String firstName, String lastName) {
        super();
        this.authorId = authorId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Override
    public String getEventType() {
        return eventType;
    }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }

    // getters...
    public String getAuthorId() {
        return authorId;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public AuthorCreatedEvent getAuthorCreatedEvent(AuthorDto dto) {
        UUID uuid = UUID.randomUUID();
        return new AuthorCreatedEvent(uuid.toString(), dto.getEmail(), dto.getFirstName(), dto.getLastName());
    }
}
