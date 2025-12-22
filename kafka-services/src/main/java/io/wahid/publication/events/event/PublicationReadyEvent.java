package io.wahid.publication.events.event;

import io.wahid.publication.dto.BookDto;
import io.wahid.publication.dto.MagazineDto;
import io.wahid.publication.model.events.TYPE;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class PublicationReadyEvent implements DomainEvent {
    private final String publicationId;
    private final TYPE type; // BOOK, MAGAZINE
    private final String title;
    private final String isbn;
    private final List<String> authorEmails;
    private final String description;
    private final LocalDate publicationDate;
    private final Instant occurredAt = Instant.now();
    private final String eventType = "PublicationCreated";

    public PublicationReadyEvent() {
        super();
        this.isbn = null;
        this.publicationId = null;
        this.type = null;
        this.title = null;
        this.authorEmails = null;
        this.description = null;
        this.publicationDate = null;
    }

    public PublicationReadyEvent(String publicationId, TYPE type, String title, List<String> authorEmails, String description, LocalDate publicationDate, String isbn) {
        this.publicationId = publicationId;
        this.type = type;
        this.isbn = isbn;
        this.title = title;
        this.authorEmails = authorEmails;
        this.description = description;
        this.publicationDate = publicationDate;
    }

    // Getters and Setters
    public String getPublicationId() { return publicationId; }
    public TYPE getType() { return type; }
    public String getTitle() { return title; }
    public List<String> getAuthorEmails() { return authorEmails; }
    public String getDescription() { return description; }
    public LocalDate getPublicationDate() { return publicationDate; }
    public String getIsbn() { return isbn; }

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
                ", title='" + title + '\'' +
                ", isbn='" + isbn + '\'' +
                ", authorEmails=" + authorEmails +
                ", description='" + description + '\'' +
                ", publicationDate=" + publicationDate +
                ", occurredAt=" + occurredAt +
                ", eventType='" + eventType + '\'' +
                '}';
    }

    public PublicationReadyEvent getPublicationReadyEvent(BookDto dto) {
        return new PublicationReadyEvent(UUID.randomUUID().toString(), TYPE.BOOK, dto.getTitle(), dto.getAuthorEmails(), dto.getDescription(), null, dto.getIsbn());
    }

    public PublicationReadyEvent getPublicationReadyEvent(MagazineDto dto) {
        return new PublicationReadyEvent(UUID.randomUUID().toString(), TYPE.MAGAZINE, dto.getTitle(), dto.getAuthorEmails(), null, dto.getPublicationDate(), dto.getIsbn());
    }
}
