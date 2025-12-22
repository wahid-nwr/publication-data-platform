package io.wahid.publication.dto;

import java.util.List;

public abstract class AbstractPublicationDto {
    private List<String> authors;

    public List<String> getAuthorEmails() {
        return this.authors;
    }
}
