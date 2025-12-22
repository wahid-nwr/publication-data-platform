package io.wahid.publication.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.wahid.publication.util.CustomDateSerializer;
import io.wahid.publication.util.PublicationStringUtil;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Entity
@DiscriminatorValue("MAGAZINE")
@Access(AccessType.FIELD)
public class Magazine extends PublicationModel {

    @JsonSerialize(using = CustomDateSerializer.class)
    private final LocalDate publicationDate;

    protected Magazine() {
        super();
        this.publicationDate = null;
    }

    public Magazine(String title, String isbn, List<Author> authors, LocalDate publicationDate) {
        super(title, isbn, authors);
        this.publicationDate = publicationDate;
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        if (!(o instanceof Magazine that)) return false;
        return Objects.equals(publicationDate, that.publicationDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), publicationDate);
    }

    @Override
    public String toString() {
        return "Magazine{" +
                "title='" + getTitle() + '\'' +
                ", isbn='" + getIsbn() + '\'' +
                ", authors=" + getAuthorEmails() +
                ", publication_date=" + publicationDate +
                '}';
    }

    public LocalDate getPublicationDate() {
        return this.publicationDate;
    }

    @Override
    public String toCanonicalString() {
        return super.toCanonicalString() +
                "|publication_date=" + PublicationStringUtil.normalize(publicationDate != null ? publicationDate.toString() : "");
    }
}
