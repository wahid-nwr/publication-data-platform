package io.wahid.publication.repository;

import io.wahid.publication.model.Book;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;

public class BookRepository {

    private final EntityManagerFactory emf;

    public BookRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public Long count() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT COUNT(b) FROM Book b", Long.class).getSingleResult();
        }
    }

    public List<Book> findBooks(int offset, int limit) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT b FROM Book b", Book.class)
                    .setFirstResult(offset)
                    .setMaxResults(limit)
                    .getResultList();
        }
    }
}