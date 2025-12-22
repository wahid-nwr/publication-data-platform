package io.wahid.publication.service;

import io.wahid.publication.dto.AuthorDto;
import io.wahid.publication.dto.BookDto;
import io.wahid.publication.dto.MagazineDto;
import io.wahid.publication.exception.BatchProcessingException;
import io.wahid.publication.mapper.AuthorMapper;
import io.wahid.publication.mapper.BookMapper;
import io.wahid.publication.mapper.MagazineMapper;
import io.wahid.publication.model.Author;
import io.wahid.publication.model.Book;
import io.wahid.publication.model.Magazine;
import io.wahid.publication.repository.AuthorRepository;
import io.wahid.publication.util.JpaUtil;
import com.publication.events.AuthorCreatedEvent;
import com.publication.events.PublicationReadyEvent;
import io.wahid.publication.CsvMain;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import static io.wahid.publication.KafkaConstants.*;

public class CsvProcessor {
    private static final boolean LOAD_FILE_FROM_VOLUME = CsvMain.FILE_SOURCE_VOLUME != null && CsvMain.FILE_SOURCE_VOLUME.equals("true");

    public void startParsingCsv() {
        parseAuthorCsv();
        parseBookCsv();
        parseMagazineCsv();
    }

    private void parseAuthorCsv() {
        String csvPath = System.getenv().getOrDefault("AUTHOR_CSV_PATH", "data/autoren.csv");
        String charset = System.getenv().getOrDefault("AUTHOR_CSV_CHARSET", "ISO_8859_1");
        try (InputStream in = CsvInputStreamProvider.open(csvPath, LOAD_FILE_FROM_VOLUME)) {
            CsvToDtoProducer<AuthorDto, Author, AuthorCreatedEvent> authorProducer = new CsvToDtoProducer<>(BOOTSTRAP_SERVER, AUTHOR_CREATED_TOPIC);
            authorProducer.parseCsv(csvPath, Charset.forName(charset), AuthorDto.class, in, null, new AuthorMapper());
            authorProducer.close();
        } catch (IOException e) {
            throw new BatchProcessingException(e.getMessage(), e);
        }
    }

    private void parseBookCsv() {
        String csvPath = System.getenv().getOrDefault("BOOK_CSV_PATH", "data/buecher.csv");
        String charset = System.getenv().getOrDefault("BOOK_CSV_CHARSET", "windows-1252");
        try (InputStream in = CsvInputStreamProvider.open(csvPath, LOAD_FILE_FROM_VOLUME)) {
            CsvToDtoProducer<BookDto, Book, PublicationReadyEvent> publicationProducer =
                    new CsvToDtoProducer<>(BOOTSTRAP_SERVER, PUBLICATION_CREATED_TOPIC);
            BookMapper bookMapper = new BookMapper(new AuthorRepository(JpaUtil.getEntityManagerFactory()));
            publicationProducer.parseCsv(csvPath, Charset.forName(charset), BookDto.class, in, null, bookMapper);
            publicationProducer.close();
        } catch (IOException e) {
            throw new BatchProcessingException(e);
        }
    }

    private void parseMagazineCsv() {
        String csvPath = System.getenv().getOrDefault("MAGAZINE_CSV_PATH", "data/zeitschriften.csv");
        String charset = System.getenv().getOrDefault("MAGAZINE_CSV_CHARSET", "ISO_8859_1");
        try (InputStream in = CsvInputStreamProvider.open(csvPath, LOAD_FILE_FROM_VOLUME)) {
            CsvToDtoProducer<MagazineDto, Magazine, PublicationReadyEvent> publicationProducer =
                    new CsvToDtoProducer<>(BOOTSTRAP_SERVER, PUBLICATION_CREATED_TOPIC);

            MagazineMapper magazineMapper = new MagazineMapper(new AuthorRepository(JpaUtil.getEntityManagerFactory()));
            publicationProducer.parseCsv(csvPath, Charset.forName(charset), MagazineDto.class, in, null, magazineMapper);
            publicationProducer.close();
        } catch (IOException e) {
            throw new BatchProcessingException(e);
        }
    }
}
