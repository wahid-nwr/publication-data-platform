package io.wahid.publication.dto;

import com.opencsv.bean.*;

import java.time.LocalDate;
import java.util.List;

public class MagazineDto extends AbstractPublicationDto {
    @CsvBindByPosition(position = 0, required = true)
    private final String title;
    @CsvBindByPosition(position = 1, required = true)
    private final String isbn;
    @CsvBindAndSplitByPosition(position = 2, required = true, elementType = String.class, splitOn = ",")
    private final List<String> authorEmails;
    @CsvDate(value = "dd.MM.yyyy")
    @CsvBindByPosition(position = 3, required = true)
    private final LocalDate publicationDate;

    public MagazineDto() {
        this.title = null;
        this.isbn = null;
        this.authorEmails = null;
        this.publicationDate = null;
    }

    public MagazineDto(String title, String isbn, List<String> authorEmails, LocalDate publicationDate) {
        this.title = title;
        this.isbn = isbn;
        this.authorEmails = List.copyOf(authorEmails);
        this.publicationDate = publicationDate;
    }

    @Override
    public String toString() {
        return "Title: " + title + ", ISBN: " + isbn + ", Authors: (" + authorEmails + "), published: ( "+ publicationDate +")";
    }

    public String getTitle() {
        return title;
    }

    public String getIsbn() {
        return isbn;
    }

    @Override
    public List<String> getAuthorEmails() {
        return this.authorEmails;
    }

    public LocalDate getPublicationDate() {
        return publicationDate;
    }
}
