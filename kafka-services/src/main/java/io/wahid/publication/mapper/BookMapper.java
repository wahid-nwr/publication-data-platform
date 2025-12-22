package io.wahid.publication.mapper;

import io.wahid.publication.dto.BookDto;
import io.wahid.publication.model.Author;
import io.wahid.publication.model.Book;
import io.wahid.publication.model.events.TYPE;
import io.wahid.publication.repository.AuthorRepository;
import com.publication.events.PublicationReadyEvent;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BookMapper implements DomainObjectMapper<BookDto, Book, PublicationReadyEvent> {
    private final AuthorRepository authorRepository;

    public BookMapper(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    @Override
    public final Book toEntity(BookDto source) {
        List<Author> authors = authorRepository.findByEmail(source.getAuthorEmails());
        return new Book(source.getTitle(), source.getIsbn(), authors, source.getDescription());
    }

    @Override
    public BookDto toDto(Book source) {
        List<String> authorEmails = source.getAuthors().stream().map(Author::getEmail).toList();
        return new BookDto(source.getTitle(), source.getIsbn(), authorEmails, source.getDescription());
    }

    @Override
    public PublicationReadyEvent toEvent(BookDto source) {
        PublicationReadyEvent event = new PublicationReadyEvent();
        event.setPublicationId(UUID.randomUUID().toString());
        event.setIsbn(source.getIsbn());
        event.setTitle(source.getTitle());
        event.setEventType("PublicationCreated");
        event.setType(TYPE.BOOK.name());
        event.setDescription(source.getDescription());
        event.setAuthorEmails(new ArrayList<>(source.getAuthorEmails()));
        event.setDescription(source.getDescription());
        // satisfy the schema now, remove later
        LocalDate date = LocalDate.now();
        List<Integer> dateValueList = List.of(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
        event.setPublicationDate(dateValueList);
        return event;
    }

    @Override
    public Book toEntityFromEvent(PublicationReadyEvent event) {
        List<Author> authors = authorRepository.findByEmail(event.getAuthorEmails().stream().map(CharSequence::toString).toList());
        return new Book(event.getTitle().toString(), event.getIsbn().toString(), authors, event.getDescription().toString());
    }
}
