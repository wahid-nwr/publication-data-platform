package io.wahid.publication.repository;

import io.wahid.publication.model.PublicationModel;
import io.wahid.publication.model.web3.PublicationTransaction;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;
import java.util.Optional;

public class PublicationRepository {

    private final EntityManagerFactory emf;

    public PublicationRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public Long count() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT COUNT(p) FROM PublicationModel p", Long.class).getSingleResult();
        }
    }

    public List<PublicationModel> findPublications(int offset, int limit) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT p FROM PublicationModel p ORDER BY p.title", PublicationModel.class)
                    .setFirstResult(offset)
                    .setMaxResults(limit)
                    .getResultList();
        }
    }

    public List<PublicationModel> findByEmail(String email) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT DISTINCT p FROM PublicationModel p JOIN FETCH p.authors au WHERE au.email = :email",
                            PublicationModel.class)
                    .setParameter("email", email)
                    .getResultList();
        }
    }

    public boolean doesExistWithIsbn(String isbn) {
        try (EntityManager em = emf.createEntityManager()) {
            return !em.createQuery("SELECT p FROM PublicationModel p WHERE p.isbn = :isbn", PublicationModel.class)
                    .setParameter("isbn", isbn)
                    .getResultList().isEmpty();
        }
    }

    public Optional<PublicationModel> findByIsbn(String isbn) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT p FROM PublicationModel p WHERE p.isbn = :isbn", PublicationModel.class)
                    .setParameter("isbn", isbn)
                    .getResultList().stream().findFirst();
        }
    }

    public List<PublicationModel> findByLastName(String lastName) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT DISTINCT p FROM PublicationModel p JOIN FETCH p.authors au WHERE au.lastName = :lastName",
                            PublicationModel.class)
                    .setParameter("lastName", lastName)
                    .getResultList();
        }
    }

    public Optional<PublicationModel> findById(Long id) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT p FROM PublicationModel p WHERE p.id = :id", PublicationModel.class)
                    .setParameter("id", id)
                    .getResultList().stream().findFirst();
        }
    }

    public Optional<PublicationTransaction> findWeb3TransactionByIsbn(String isbn) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT p FROM PublicationTransaction p WHERE p.isbn = :isbn", PublicationTransaction.class)
                    .setParameter("isbn", isbn)
                    .getResultList().stream().findFirst();
        }
    }
}
