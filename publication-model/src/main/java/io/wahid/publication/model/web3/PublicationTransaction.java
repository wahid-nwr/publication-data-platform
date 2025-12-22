package io.wahid.publication.model.web3;

import jakarta.persistence.*;

@Entity
@Table(name = "publication_transactions", uniqueConstraints = {@UniqueConstraint(columnNames = {"isbn"})})
@Access(AccessType.FIELD)
public class PublicationTransaction {
    @Column(unique = true, nullable = false)
    private final String isbn;
    @Column(nullable = false)
    private final String transaction;
    @Id
    @Column(nullable = false)
    private String id;

    protected PublicationTransaction() {
        this.isbn = null;
        this.transaction = null;
    }

    public PublicationTransaction(String id, String isbn, String transaction) {
        this.id = id;
        this.isbn = isbn;
        this.transaction = transaction;
    }

    public String getIsbn() {
        return this.isbn;
    }

    public String getTransaction() {
        return this.transaction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PublicationTransaction that = (PublicationTransaction) o;
        return isbn != null && isbn.equals(that.isbn);
    }
}
