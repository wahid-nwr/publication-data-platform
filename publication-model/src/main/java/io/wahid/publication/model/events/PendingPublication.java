package io.wahid.publication.model.events;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "pending_publications", uniqueConstraints = {@UniqueConstraint(columnNames = {"isbn"})})
@Access(AccessType.FIELD)
public class PendingPublication {
    @Id
    @Column(nullable = false)
    private String id;
    @Column(nullable = false)
    private final String title;
    @Column(unique = true, nullable = false)
    private final String isbn;
    private final List<String> authorEmails;
    @Lob
    @Column(columnDefinition = "text")
    private String description;
    private LocalDate publicationDate;
    @Enumerated(EnumType.STRING)
    private TYPE type;
    @Enumerated(EnumType.STRING)
    private Status status;

    protected PendingPublication() {
        this.title = null;
        this.isbn = null;
        this.authorEmails = null;
    }

    public PendingPublication(String bookId, String title, String isbn, List<String> authorEmails, String description, Status status) {
        this.id = bookId;
        this.title = title;
        this.isbn = isbn;
        this.authorEmails = List.copyOf(authorEmails);
        this.description = description;
        this.status = status;
        this.type = TYPE.BOOK;
    }

    public PendingPublication(String magazineId, String title, String isbn, List<String> authorEmails, LocalDate publicationDate, Status status) {
        this.id = magazineId;
        this.title = title;
        this.isbn = isbn;
        this.authorEmails = List.copyOf(authorEmails);
        this.publicationDate = publicationDate;
        this.status = status;
        this.type = TYPE.MAGAZINE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PendingPublication that = (PendingPublication) o;
        return isbn != null && isbn.equals(that.isbn);
    }

    @Override
    public int hashCode() {
        return isbn != null ? isbn.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "PendingPublication{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", isbn='" + isbn + '\'' +
                ", authorEmails=" + authorEmails +
                ", description='" + description + '\'' +
                ", publicationDate=" + publicationDate +
                ", type=" + type +
                ", status=" + status +
                '}';
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return this.title;
    }

    public String getIsbn() {
        return this.isbn;
    }

    public List<String> getAuthorEmails() {
        return List.copyOf(this.authorEmails);
    }

    public String getDescription() { return this.description; }

    public LocalDate getPublicationDate() { return this.publicationDate; }

    public TYPE getType() { return this.type; }

    public Status getStatus() { return this.status; }

    public void setStatus(Status status) {
        this.status = status;
    }
}
