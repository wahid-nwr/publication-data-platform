package io.wahid.publication.service;

import io.wahid.publication.model.Author;
import io.wahid.publication.model.Book;
import io.wahid.publication.model.Magazine;
import io.wahid.publication.model.PublicationModel;
import io.wahid.publication.dto.AuthorDto;
import io.wahid.publication.dto.BookDto;
import io.wahid.publication.dto.MagazineDto;
import io.wahid.publication.model.web3.PublicationTransaction;

import java.util.List;

public interface IPublicationManager {
    /**
     * @deprecated Provides new methods for handling data.
     * Use {@link #getAuthors(int, int)} instead.
     */
    @Deprecated(since = "1.1")
    List<AuthorDto> getAuthors();
    /**
     * @deprecated Provides new methods for handling data.
     * Use {@link #getBooks(int, int)} instead.
     */
    @Deprecated(since = "1.1")
    List<BookDto> getBooks();
    /**
     * @deprecated Provides new methods for handling data.
     * Use {@link #getMagazines(int, int)} instead.
     */
    @Deprecated(since = "1.1")
    List<MagazineDto> getMagazines();
    /**
     * @deprecated Provides new methods for handling data.
     * Use {@link #getAllPublications(int, int)} instead.
     */
    @Deprecated(since = "1.1")
    List<PublicationModel> getAllPublications();

    long countAuthors();
    long countBooks();
    long countMagazines();
    long countPublications();
    List<Author> getAuthors(int offset, int limit);
    List<Book> getBooks(int offset, int limit);
    List<Magazine> getMagazines(int offset, int limit);
    List<PublicationModel> getAllPublications(int offset, int limit);

    List<PublicationModel> getPublicationsByAuthor(String email);
    PublicationModel getPublicationByISBN(String isbn);
    List<PublicationModel> getPublicationsByAuthorLastName(String lastName);
    PublicationModel getPublicationById(Long id);
    PublicationTransaction getWeb3TransactionByIsbn(String isbn);
}
