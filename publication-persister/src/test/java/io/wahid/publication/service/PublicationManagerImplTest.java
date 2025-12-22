package io.wahid.publication.service;

import io.wahid.publication.dto.AuthorDto;
import io.wahid.publication.dto.BookDto;
import io.wahid.publication.dto.MagazineDto;
import io.wahid.publication.mapper.AuthorMapper;
import io.wahid.publication.mapper.BookMapper;
import io.wahid.publication.mapper.MagazineMapper;
import io.wahid.publication.model.Author;
import io.wahid.publication.model.Book;
import io.wahid.publication.model.Magazine;
import io.wahid.publication.model.PublicationModel;
import io.wahid.publication.repository.AuthorRepository;
import io.wahid.publication.service.impl.PublicationManagerImpl;
import io.wahid.publication.util.JpaUtil;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PublicationManagerImplTest {

    private static final int OFFSET = 0;
    private static final int LIMIT = 50;

    private IPublicationManager publicationManager;
    private AuthorMapper authorMapper;
    private BookMapper bookMapper;
    private MagazineMapper magazineMapper;
    private AuthorRepository authorRepository;

    @BeforeAll
    void setUp() throws IOException {
        EntityManagerFactory emf = JpaUtil.getEntityManagerFactory();
        publicationManager = PublicationManagerImpl.createInitialized();
        authorRepository = new AuthorRepository(emf);
        authorMapper = new AuthorMapper();
        bookMapper = new BookMapper(authorRepository);
        magazineMapper = new MagazineMapper(authorRepository);
    }

    @DisplayName("Should throw exception for null ISBN")
    @Test
    void getPublicationByISBN_withNull_returnsNull() {
        Exception exception = assertThrows(
                jakarta.persistence.NoResultException.class,
                () -> publicationManager.getPublicationByISBN(null));
        assertEquals("No result found for query [SELECT p FROM PublicationModel p WHERE p.isbn = :isbn]",
                exception.getMessage());
    }

    @DisplayName("Should find publications by author email")
    @ParameterizedTest
    @CsvSource({
            "pr-walter@optivo.de, true, 6, Expected 6 publications for pr-walter@optivo.de",
            "wahid@test.test, true, 0, There should be no publications for wahid@test.test",
            "wahid@test.test, false, 1, There should be no publications for wahid@test.test",
    })
    void getPublicationByAuthor_withEmail_returnsListOfPublication(String email, boolean isExpected, Integer size, String message) {
        List<PublicationModel> publications = publicationManager.getPublicationsByAuthor(email);
        if (isExpected) {
            assertEquals(size, publications.size(), message);
        } else {
            assertNotEquals(size, publications.size(), message);
        }
    }

    @DisplayName("Should check if all books returned from the csv file")
    @ParameterizedTest
    @CsvSource({
            "8, true, There should be 8 books from the csv file",
            "12, false, There should be 8 books not 12",
    })
    void getBooks_returnsAllBooks(Integer size, boolean isExpected, String message) {
        List<Book> books = publicationManager.getBooks(OFFSET, LIMIT);
        long count = publicationManager.countBooks();
        assertFalse(books.isEmpty(), "Book list should not be empty");
        if (isExpected) {
            assertEquals(size, books.size(), message);
            assertEquals(size, (int) count, message);
        } else {
            assertNotEquals(size, books.size(), message);
        }
    }

    @DisplayName("Should check if all magazines returned from the csv file")
    @ParameterizedTest
    @CsvSource({
            "6, 9, true, There should be 6 magazines from the csv file",
            "10, 0, false, There should be 6 magazines not 10",
    })
    void getMagazines_returnsAllMagazines(Integer size, Long id, boolean isExpected, String message) {
        List<Magazine> magazines = publicationManager.getMagazines(OFFSET, LIMIT);
        long count = publicationManager.countMagazines();
        assertFalse(magazines.isEmpty(), "Magazine list should not be empty");
        if (isExpected) {
            assertEquals(size, magazines.size(), message);
            assertEquals((int) size, count, message);
            Magazine magazine1 = magazines.get(0);
            PublicationModel publication = publicationManager.getPublicationById(id);
            assertEquals(magazine1.getId(), publication.getId());
            assertEquals(magazine1.hashCode(), publication.hashCode());

            Magazine magazine2 = (Magazine) publication;
            assertEquals(magazine1.hashCode(), magazine2.hashCode());
            assertEquals(magazine1.toString(), magazine2.toString());
            assertEquals(magazine1, magazine2);
            MagazineDto magazineDto1 = magazineMapper.toDto(magazine1);
            MagazineDto magazineDto2 = magazineMapper.toDto(magazine2);
            assertEquals(magazineDto1.toString(), magazineDto2.toString());
        } else {
            assertNotEquals(size, magazines.size(), message);
        }
    }

    @DisplayName("Should check if all publications both books and magazines returned from the csv files")
    @ParameterizedTest
    @CsvSource({
            "14, true, There are 14 publications in total",
            "10, false, 10 is wrong as number of publications in the list",
    })
    void getAllPublications_returnsAllPublications(Integer size, boolean isExpected, String message) {
        List<PublicationModel> publications = publicationManager.getAllPublications(OFFSET, LIMIT);
        assertFalse(publications.isEmpty(), "Publication list should not be empty");
        if (isExpected) {
            assertEquals(size, publications.size(), message);
        } else {
            assertNotEquals(size, publications.size(), message);
        }
    }

    @DisplayName("Should check if all publications persisted to db from the csv files")
    @ParameterizedTest
    @CsvSource({
            "14, true, There are 6 magazines and 8 books provided in two files",
            "10, false, 10 is wrong as number of publications in the list",
    })
    void getPublicationCount_returnsPublicationsCount(Long size, boolean isExpected, String message) {
        long publicationsCount = publicationManager.countPublications();
        assertNotEquals(0, publicationsCount, "Publication list count should not be zero");
        if (isExpected) {
            assertEquals(size, publicationsCount, message);
        } else {
            assertNotEquals(size, publicationsCount, message);
        }
    }

    @DisplayName("Should check the ordering of the cumulative publication list")
    @ParameterizedTest
    @CsvSource({
            "Das Perfekte Dinner. Die besten Rezepte, 0, true, The first publication title should start with Das",
            "Vinum, 13, true, The last publication title should be Vinum",
            "Z-Fake-Title, 0, false, This should fail if the list is not correctly ordered"
    })
    void getAllPublications_returnsAllPublicationsSortedByTitle(String title, int index, boolean isExpected, String message) {
        List<PublicationModel> publications = publicationManager.getAllPublications(OFFSET, LIMIT);
        assertFalse(publications.isEmpty(), "Publication list should not be empty");
        if (isExpected) {
            assertEquals(title, publications.get(index).getTitle(), message);
        } else {
            assertNotEquals(title, publications.get(index).getTitle(), message);
        }
    }

    @DisplayName("Should check if all authors persisted to db from the csv files")
    @ParameterizedTest
    @CsvSource({
            "6, true, There are 6 authors provided in the file",
            "10, false, 10 is wrong as number of authors in the list",
    })
    void getAuthorsCount_returnsAuthorsCount(Long size, boolean isExpected, String message) {
        long authorsCount = publicationManager.countAuthors();
        assertNotEquals(0, authorsCount, "Author count should not be zero");
        if (isExpected) {
            assertEquals(size, authorsCount, message);
        } else {
            assertNotEquals(size, authorsCount, message);
        }
    }

    @DisplayName("Should check if all authors returned from the db")
    @ParameterizedTest
    @CsvSource({
            "6, 1, true, There are 6 authors provided in the file",
            "10, 0, false, 10 is wrong as number of authors in the list",
    })
    void getAuthors_returnsAllAuthors(Integer size, Long id, boolean isExpected, String message) {
        List<Author> authors = publicationManager.getAuthors(OFFSET, LIMIT);
        assertFalse(authors.isEmpty(), "Author list should not be empty");
        if (isExpected) {
            assertEquals(size, authors.size(), message);
            Author author1 = authors.get(0);
            Author author2 = authorRepository.findById(id);
            assertEquals(author1.getEmail(), author2.getEmail());
            assertEquals(author1.hashCode(), author2.hashCode());
            assertEquals(author1.toString(), author2.toString());
            assertEquals(author1.getId(), author2.getId());
            assertEquals(author1.getEmail(), author2.getEmail());
            assertEquals(author1, author2);
            assertNotEquals(null, author1);
            assertNotEquals(author1, new Object());
            AuthorDto authorDto1 = authorMapper.toDto(author1);
            AuthorDto authorDto2 = authorMapper.toDto(author2);
            assertEquals(authorDto2.toString(), authorDto1.toString());
        } else {
            assertNotEquals(size, authors.size(), message);
        }
    }

    @DisplayName("Should find publication by ISBN or throw exception")
    @ParameterizedTest
    @CsvSource({
            "1024-5245-8584, 6, true, Publication should be found with given isbn '1024-5245-8584'",
            "test, 0, false, There should be no publication with the isbn 'test'",
    })
    void getPublicationByISBN_withISBN_returnsPublicationOrNull(String isbn, Long id, boolean isExpected, String message) {
        if (isExpected) {
            PublicationModel publication = publicationManager.getPublicationByISBN(isbn);
            PublicationModel publicationById = publicationManager.getPublicationById(id);
            assertNotNull(publication, message);
            assertEquals(publicationById.hashCode(), publication.hashCode());
            assertEquals(publicationById.toString(), publication.toString());
            assertEquals(publicationById, publication);

            Book book1 = (Book) publicationById;
            Book book2 = (Book) publication;
            assertEquals(book1.hashCode(), book2.hashCode());
            assertEquals(book1.toString(), book2.toString());
            assertEquals(book1, book2);
            BookDto bookDto1 = bookMapper.toDto(book1);
            BookDto bookDto2 = bookMapper.toDto(book2);
            assertEquals(bookDto1.toString(), bookDto2.toString());
        } else {
            Exception exception = assertThrows(
                    jakarta.persistence.NoResultException.class,
                    () -> publicationManager.getPublicationByISBN(isbn));
            assertEquals("No result found for query [SELECT p FROM PublicationModel p WHERE p.isbn = :isbn]",
                    exception.getMessage());
        }
    }

    @DisplayName("Should find publication by ISBN or throw exception")
    @ParameterizedTest
    @CsvSource({
            "Walter, 6, true, 6 publications should be found with given author last name 'Walter'",
            "test, 1, false, There should be no publication with the author last name 'test'",
    })
    void getPublicationsByAuthorLastName_withISBN_returnsPublicationsOrEmpty(String lastName, int size,
                                                                             boolean isExpected, String message) {
        List<PublicationModel> publications = publicationManager.getPublicationsByAuthorLastName(lastName);
        if (isExpected) {
            assertEquals(size, publications.size(), message);
        } else {
            assertNotEquals(size, publications.size(), message);
        }
    }
}