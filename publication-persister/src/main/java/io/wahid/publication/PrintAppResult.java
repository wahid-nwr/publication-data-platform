package io.wahid.publication;

import io.wahid.publication.model.Book;
import io.wahid.publication.model.Magazine;
import io.wahid.publication.model.PublicationModel;
import io.wahid.publication.service.impl.PublicationManagerImpl;
import io.wahid.publication.util.AppConfig;

import java.io.IOException;
import java.util.logging.Logger;

import static java.util.logging.Level.*;

public class PrintAppResult {
    private static final Logger LOGGER = Logger.getLogger(PrintAppResult.class.getName());
    private static final int LIMIT = AppConfig.getIntProperty("print.limit");
    private final PublicationManagerImpl publicationManager;

    public PrintAppResult() throws IOException {
        publicationManager = PublicationManagerImpl.createInitialized();
    }

    public void print() {
        String authorEmail = "pr-walter@optivo.de";
        String sampleIsbn = "1024-5245-8584";
        String sampleLastName = "Walter";

        try {
            LOGGER.log(INFO, "---- ALL AUTHORS ----");
            long authorsCount = LIMIT < publicationManager.countAuthors() ? LIMIT : publicationManager.countAuthors() ;
            LOGGER.log(INFO, "Total authors: {0}, printing first {1}", new Object[]{ publicationManager.countAuthors(), authorsCount});
            for (int offset = 0; offset < authorsCount; offset += LIMIT) {
                publicationManager.getAuthors(offset, LIMIT).forEach(author -> LOGGER.log(INFO, "{0}", author));
            }

            LOGGER.log(INFO,"---- ALL PUBLICATIONS ----");
            long publicationsCount = publicationManager.countPublications();
            for (int offset = 0; offset < publicationsCount; offset += LIMIT) {
                for (PublicationModel pub : publicationManager.getAllPublications(offset, LIMIT)) {
                    LOGGER.log(INFO, "{0}", new Object[]{pub});
                }
            }

            LOGGER.log(INFO,"---- ALL BOOKS ----");
            long booksCount = publicationManager.countBooks();
            for (int offset = 0; offset < booksCount; offset += LIMIT) {
                for (Book book : publicationManager.getBooks(offset, LIMIT)) {
                    LOGGER.log(INFO, "{0}", new Object[]{book});
                }
            }

            LOGGER.log(INFO,"---- ALL MAGAZINES ----");
            long magazinesCount = publicationManager.countMagazines();
            for (int offset = 0; offset < magazinesCount; offset += LIMIT) {
                for (Magazine magazine : publicationManager.getMagazines(offset, LIMIT)) {
                    LOGGER.log(INFO, "{0}", new Object[]{magazine});
                }
            }

            LOGGER.log(INFO,"---- PUBLICATIONS BY AUTHOR EMAIL----");
            for (PublicationModel pub : publicationManager.getPublicationsByAuthor(authorEmail)) {
                LOGGER.log(INFO, "Publication by author email, {0}: {1}", new Object[]{authorEmail, pub});
            }

            LOGGER.log(INFO,"---- PUBLICATION BY ISBN ----");
            LOGGER.log(INFO, "Publication by ISBN {0}: {1}", new Object[]{ sampleIsbn, publicationManager.getPublicationByISBN(sampleIsbn)});

            LOGGER.log(INFO, "---- PUBLICATION BY LAST NAME ----");
            for (PublicationModel pub : publicationManager.getPublicationsByAuthorLastName(sampleLastName)) {
                LOGGER.log(INFO, "Publication by last name, {0}: {1}", new Object[]{sampleLastName, pub});
            }
        } catch (Exception e) {
            LOGGER.log(WARNING, "Unexpected error in application execution", e);
        }
    }
}
