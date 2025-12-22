package io.wahid.publication.mapper;

import io.wahid.publication.dto.MagazineDto;
import io.wahid.publication.model.Author;
import io.wahid.publication.model.Magazine;
import io.wahid.publication.model.events.TYPE;
import io.wahid.publication.repository.AuthorRepository;
import com.publication.events.PublicationReadyEvent;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MagazineMapper implements DomainObjectMapper<MagazineDto, Magazine, PublicationReadyEvent> {
    private final AuthorRepository authorRepository;

    public MagazineMapper(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    @Override
    public final Magazine toEntity(MagazineDto source) {
        List<Author> authors = authorRepository.findByEmail(source.getAuthorEmails());
        return new Magazine(source.getTitle(), source.getIsbn(), authors, source.getPublicationDate());
    }

    @Override
    public MagazineDto toDto(Magazine source) {
        List<String> authorEmails = source.getAuthors().stream().map(Author::getEmail).toList();
        return new MagazineDto(source.getTitle(), source.getIsbn(), authorEmails, source.getPublicationDate());
    }

    @Override
    public PublicationReadyEvent toEvent(MagazineDto source) {
        PublicationReadyEvent event = new PublicationReadyEvent();
        event.setPublicationId(UUID.randomUUID().toString());
        event.setIsbn(source.getIsbn());
        event.setTitle(source.getTitle());
        event.setEventType("PublicationCreated");
        event.setType(TYPE.MAGAZINE.name());
        event.setAuthorEmails(new ArrayList<>(source.getAuthorEmails()));
        LocalDate date = source.getPublicationDate() != null ? source.getPublicationDate() : LocalDate.now();
        List<Integer> dateValueList = List.of(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
        event.setPublicationDate(dateValueList);
        // satisfy for now
        event.setDescription(source.getTitle());
        return event;
    }

    @Override
    public Magazine toEntityFromEvent(PublicationReadyEvent event) {
        List<Author> authors = authorRepository.findByEmail(event.getAuthorEmails().stream().map(CharSequence::toString).toList());
        LocalDate date = LocalDate.of(event.getPublicationDate().get(0), event.getPublicationDate().get(1), event.getPublicationDate().get(2));
        return new Magazine(event.getTitle().toString(), event.getIsbn().toString(), authors, date);
    }
}
