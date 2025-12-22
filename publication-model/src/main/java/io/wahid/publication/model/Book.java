package io.wahid.publication.model;

import io.wahid.publication.util.PublicationStringUtil;
import jakarta.persistence.*;

import java.util.List;
import java.util.Objects;

@Entity
@DiscriminatorValue("BOOK")
@Access(AccessType.FIELD)
public class Book extends PublicationModel {
    @Lob
    @Column(columnDefinition = "text")
    private final String description;

    protected Book() {
        super();
        this.description = null;
    }

    public Book(String title, String isbn, List<Author> authors, String description) {
        super(title, isbn, authors);
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        if (!(o instanceof Book that)) return false;
        return Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), description);
    }

    @Override
    public String toString() {
        return "Book{" +
                "title='" + getTitle() + '\'' +
                ", isbn='" + getIsbn() + '\'' +
                ", authors=" + getAuthorEmails() +
                ", description='" + description + '\'' +
                '}';
    }

    public String getDescription() {
        return this.description;
    }

    @Override
    public String toCanonicalString() {
        return super.toCanonicalString() +
                "|description=" + PublicationStringUtil.normalize(description);
    }
}
