package io.wahid.publication.service.impl;

import io.wahid.publication.dto.AuthorDto;
import io.wahid.publication.dto.BookDto;
import io.wahid.publication.dto.MagazineDto;
import io.wahid.publication.exception.ResourceNotFoundException;
import io.wahid.publication.mapper.AuthorMapper;
import io.wahid.publication.mapper.BookMapper;
import io.wahid.publication.mapper.MagazineMapper;
import io.wahid.publication.model.Author;
import io.wahid.publication.model.Book;
import io.wahid.publication.model.Magazine;
import io.wahid.publication.model.PublicationModel;
import io.wahid.publication.model.web3.PublicationTransaction;
import io.wahid.publication.repository.AuthorRepository;
import io.wahid.publication.repository.BookRepository;
import io.wahid.publication.repository.MagazineRepository;
import io.wahid.publication.repository.PublicationRepository;
import io.wahid.publication.service.IPublicationManager;
import io.wahid.publication.util.AppConfig;
import io.wahid.publication.util.JpaUtil;
import jakarta.persistence.EntityManagerFactory;

import java.util.Collections;
import java.util.List;

public class PublicationManagerImpl implements IPublicationManager {
    private static final int DEFAULT_PAGE_SIZE = AppConfig.getIntProperty("page.size");
    private static final int DEFAULT_OFFSET = 0;

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;
    private final MagazineRepository magazineRepository;
    private final PublicationRepository publicationRepository;
    private final AuthorMapper authorMapper;
    private final BookMapper bookMapper;
    private final MagazineMapper magazineMapper;

    public PublicationManagerImpl() {
        EntityManagerFactory emf = JpaUtil.getEntityManagerFactory();
        this.authorRepository = new AuthorRepository(emf);
        this.bookRepository = new BookRepository(emf);
        this.magazineRepository = new MagazineRepository(emf);
        this.publicationRepository = new PublicationRepository(emf);
        this.authorMapper = new AuthorMapper();
        this.bookMapper = new BookMapper(authorRepository);
        this.magazineMapper = new MagazineMapper(authorRepository);
    }

    public static PublicationManagerImpl createInitialized() {
        return new PublicationManagerImpl();
    }

    @Override
    public long countAuthors() {
        return authorRepository.count();
    }

    @Override
    public long countBooks() {
        return bookRepository.count();
    }

    @Override
    public long countMagazines() {
        return magazineRepository.count();
    }

    @Override
    public long countPublications() {
        return publicationRepository.count();
    }

    @Override
    public List<Author> getAuthors(int offset, int limit) {
        return Collections.unmodifiableList(authorRepository.findAuthors(offset, limit));
    }

    @Override
    public List<Book> getBooks(int offset, int limit) {
        return Collections.unmodifiableList(bookRepository.findBooks(offset, limit));
    }

    @Override
    public List<Magazine> getMagazines(int offset, int limit) {
        return Collections.unmodifiableList(magazineRepository.findMagazines(offset, limit));
    }

    @Override
    public List<PublicationModel> getAllPublications(int offset, int limit) {
        return Collections.unmodifiableList(publicationRepository.findPublications(offset, limit));
    }

    @Override
    public List<PublicationModel> getPublicationsByAuthor(String email) {
        List<PublicationModel> publications = publicationRepository.findByEmail(email);
        return Collections.unmodifiableList(publications);
    }

    @Override
    public PublicationModel getPublicationByISBN(String isbn) {
        return publicationRepository.findByIsbn(isbn).orElseThrow(() ->
                new ResourceNotFoundException("Publication not found for ISBN: " + isbn));
    }

    @Override
    public List<PublicationModel> getPublicationsByAuthorLastName(String name) {
        return Collections.unmodifiableList(publicationRepository.findByLastName(name));
    }

    @Override
    public List<AuthorDto> getAuthors() {
        return authorRepository.findAuthors(DEFAULT_OFFSET, DEFAULT_PAGE_SIZE).stream().map(authorMapper::toDto).toList();
    }

    @Override
    public List<BookDto> getBooks() {
        List<Book> books = bookRepository.findBooks(DEFAULT_OFFSET, DEFAULT_PAGE_SIZE);
        return books.stream().map(bookMapper::toDto).toList();
    }

    @Override
    public List<MagazineDto> getMagazines() {
        List<Magazine> magazines = magazineRepository.findMagazines(DEFAULT_OFFSET, DEFAULT_PAGE_SIZE);
        return magazines.stream().map(magazineMapper::toDto).toList();
    }

    @Override
    public List<PublicationModel> getAllPublications() {
        return Collections.unmodifiableList(publicationRepository.findPublications(DEFAULT_OFFSET, DEFAULT_PAGE_SIZE));
    }

    @Override
    public PublicationModel getPublicationById(Long id) {
        return publicationRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Publication not found for ID: " + id));
    }

    public PublicationTransaction getWeb3TransactionByIsbn(String isbn) {
        return publicationRepository.findWeb3TransactionByIsbn(isbn).orElseThrow(() ->
                new ResourceNotFoundException("PublicationTransaction not found for ISBN: " + isbn));
    }
}
