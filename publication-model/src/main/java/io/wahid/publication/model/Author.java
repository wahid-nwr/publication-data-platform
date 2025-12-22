package io.wahid.publication.model;

import com.opencsv.bean.CsvBindByPosition;
import io.wahid.publication.util.PublicationStringUtil;
import jakarta.persistence.*;

@Entity
@Table(name = "authors", uniqueConstraints = {@UniqueConstraint(columnNames = {"email"})})
@Access(AccessType.FIELD)
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
//    @SequenceGenerator(name = "author_seq", allocationSize = 1)
    private Long id;
    @CsvBindByPosition(position = 0, required = true)
    @Column(unique = true, nullable = false)
    private final String email;
    @CsvBindByPosition(position = 1, required = true)
    @Column(nullable = false)
    private final String firstName;
    @CsvBindByPosition(position = 2, required = true)
    @Column(nullable = false)
    private final String lastName;

    protected Author() {
        this.email = null;
        this.firstName = null;
        this.lastName = null;
    }

    public Author(String email, String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    @Override
    public String toString() {
        return "Author (" + id + "): " + firstName + " " + lastName + " (email : " + email + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Author author = (Author) o;
        return email != null && email.equals(author.email);
    }

    @Override
    public int hashCode() {
        return email != null ? email.hashCode() : 0;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public long getId() {
        return id;
    }

    public String toCanonicalString() {
        // Skip ID, only include meaningful fields
        return "Author:" + PublicationStringUtil.normalize(firstName) + " " + PublicationStringUtil.normalize(lastName) + " (email:" + PublicationStringUtil.normalize(email) + ")";
    }
}