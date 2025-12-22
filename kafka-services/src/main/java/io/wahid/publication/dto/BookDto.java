package io.wahid.publication.dto;

import com.opencsv.bean.CsvBindAndSplitByPosition;
import com.opencsv.bean.CsvBindByPosition;

import java.util.List;

public class BookDto extends AbstractPublicationDto {
    @CsvBindByPosition(position = 0, required = true)
    private String title;

    @CsvBindByPosition(position = 1, required = true)
    private String isbn;

    @CsvBindAndSplitByPosition(position = 2, required = true, elementType = String.class, splitOn = ",")
    private List<String> authorEmails;

    @CsvBindByPosition(position = 3, required = true)
    private String description;

    @Override
    public String toString() {
        return "Title: " + title + ", ISBN: " + isbn + ", Authors: (" + authorEmails + "), desc: ( "+ description +")";
    }

    public BookDto() {}

    public BookDto(String title, String isbn, List<String> authorEmails, String description) {
        this.title = title;
        this.isbn = isbn;
        this.authorEmails = List.copyOf(authorEmails);
        this.description = description;
    }

    public String getTitle() {
        return this.title;
    }

    public String getIsbn() {
        return this.isbn;
    }

    @Override
    public List<String> getAuthorEmails() {
        return this.authorEmails;
    }

    public String getDescription() {
        return this.description;
    }
}