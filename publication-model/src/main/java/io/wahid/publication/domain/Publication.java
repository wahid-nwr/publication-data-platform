package io.wahid.publication.domain;

import io.wahid.publication.model.Author;

import java.util.List;

public interface Publication {
    String getTitle();

    String getIsbn();

    List<String> getAuthorEmails();

    List<Author> getAuthors();
}
