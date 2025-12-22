package io.wahid.publication.mapper;

import io.wahid.publication.dto.AuthorDto;
import com.publication.events.AuthorCreatedEvent;
import io.wahid.publication.model.Author;

import java.util.UUID;

public class AuthorMapper implements DomainObjectMapper<AuthorDto, Author, AuthorCreatedEvent> {

    @Override
    public final Author toEntity(AuthorDto source) {
        return new Author(source.getEmail(), source.getFirstName(), source.getLastName());
    }

    @Override
    public AuthorDto toDto(Author source) {
        return new AuthorDto(source.getEmail(), source.getFirstName(), source.getLastName());
    }

    @Override
    public AuthorCreatedEvent toEvent(AuthorDto source) {
        AuthorCreatedEvent event = new AuthorCreatedEvent();
        event.setAuthorId(UUID.randomUUID().toString());
        event.setEventType("AuthorCreated");
        event.setEmail(source.getEmail());
        event.setFirstName(source.getFirstName());
        event.setLastName(source.getLastName());
        return event;
    }

    @Override
    public Author toEntityFromEvent(AuthorCreatedEvent event) {
        return new Author(event.getEmail().toString(), event.getFirstName().toString(), event.getLastName().toString());
    }
}
