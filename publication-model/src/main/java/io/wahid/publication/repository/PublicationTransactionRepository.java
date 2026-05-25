package io.wahid.publication.repository;

import io.wahid.publication.model.Book;
import io.wahid.publication.model.PublicationModel;
import io.wahid.publication.model.web3.PublicationTransaction;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;

public class PublicationTransactionRepository {

    private final EntityManagerFactory emf;

    public PublicationTransactionRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public Long count() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT COUNT(pbt) FROM PublicationTransaction pbt", Long.class).getSingleResult();
        }
    }

    public List<PublicationTransaction> findPublicationTransactions(int offset, int limit) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT pbt FROM PublicationTransaction pbt", PublicationTransaction.class)
                    .setFirstResult(offset)
                    .setMaxResults(limit)
                    .getResultList();
        }
    }

    public boolean isbnNotExists(String isbn) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT p FROM PublicationTransaction p WHERE p.isbn = :isbn", PublicationTransaction.class)
                    .setParameter("isbn", isbn)
                    .getResultList().isEmpty();
        }
    }
}