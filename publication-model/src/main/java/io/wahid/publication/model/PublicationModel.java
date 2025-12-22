package io.wahid.publication.model;

import io.wahid.publication.domain.Publication;
import io.wahid.publication.util.PublicationStringUtil;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "publication_type", discriminatorType = DiscriminatorType.STRING)
@Access(AccessType.FIELD)
public abstract class PublicationModel implements Publication {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
//    @SequenceGenerator(name = "publication_seq", allocationSize = 1)
    private Long id;
    @Column(nullable = false)
    private final String title;
    @Column(unique = true, nullable = false)
    private final String isbn;
//    @ManyToMany
    @ManyToMany
    /*@JoinTable(
            name = "publication_authors",
            joinColumns = @JoinColumn(name = "publication_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id")
    )*/
    private List<Author> authors;

    protected PublicationModel() {
        this.title = null;
        this.isbn = null;
        this.authors = null;
    }

    protected PublicationModel(String title, String isbn, List<Author> authors) {
        this.title = title;
        this.isbn = isbn;
        this.authors = new ArrayList<>(authors);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PublicationModel that = (PublicationModel) o;
        return isbn != null && isbn.equals(that.isbn);
    }

    @Override
    public int hashCode() {
        return isbn != null ? isbn.hashCode() : 0;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return this.title;
    }

    public String getIsbn() {
        return this.isbn;
    }

    public List<String> getAuthorEmails() {
        return this.getAuthors().stream().map(Author::getEmail).toList();
    }

    public List<Author> getAuthors() {
        return List.copyOf(this.authors);
    }

    public String toCanonicalString() {
        // Join authors deterministically
        String authorsStr = (authors == null || authors.isEmpty()) ? "" : authors.stream()
                .map(Author::toCanonicalString)
                .map(String::trim)
                .sorted() // deterministic order
                .collect(Collectors.joining(","));

        return "title=" + PublicationStringUtil.normalize(title) +
                "|isbn=" + PublicationStringUtil.normalize(isbn) +
                "|authors=" + authorsStr;
    }
}
