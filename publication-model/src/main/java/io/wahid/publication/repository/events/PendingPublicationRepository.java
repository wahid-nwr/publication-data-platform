package io.wahid.publication.repository.events;

import io.wahid.publication.model.events.PendingPublication;
import io.wahid.publication.model.events.Status;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;

public class PendingPublicationRepository {

    private final EntityManagerFactory emf;

    public PendingPublicationRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public void save(PendingPublication publication) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(publication);
            em.getTransaction().commit();
        }
    }

    public PendingPublication update(PendingPublication pub) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            pub = em.merge(pub);
            em.getTransaction().commit();
        }
        return pub;
    }

    public void updateStatus(String isbn, Status status) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.createQuery("UPDATE PendingPublication p SET p.status = :status WHERE p.isbn = :isbn")
                    .setParameter("status", status)
                    .setParameter("isbn", isbn)
                    .executeUpdate();
            em.getTransaction().commit();
        }
    }

    public Long count() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT COUNT(p) FROM PendingPublication p", Long.class).getSingleResult();
        }
    }

    public List<PendingPublication> findByAuthor(String authorEmail) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT DISTINCT p FROM PendingPublication p " +
                            "WHERE :authorEmail IN(p.authorEmails) AND status = 'PENDING'", PendingPublication.class)
                    .setParameter("authorEmail", authorEmail)
                    .getResultList();
        }
    }

    public List<PendingPublication> findByStatus(Status status, int offset, int limit) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT DISTINCT p FROM PendingPublication p WHERE p.status = :status", PendingPublication.class)
                    .setParameter("status", status)
                    .setFirstResult(offset)
                    .setMaxResults(limit)
                    .getResultList();
        }
    }

    public boolean hasPending() {
        try (EntityManager em = emf.createEntityManager()) {
            long count = em.createQuery("SELECT COUNT(p) FROM PendingPublication p WHERE p.status = :status", Long.class)
                    .setParameter("status", Status.PENDING)
                    .getSingleResult();
            return count > 0;
        }
    }

    public boolean doesExistWithIsbn(String isbn) {
        try (EntityManager em = emf.createEntityManager()) {
            long count = em.createQuery("SELECT COUNT(p) FROM PendingPublication p WHERE p.isbn = :isbn", Long.class)
                    .setParameter("isbn", isbn)
                    .getSingleResult();
            return count > 0;
        }
    }

    public PendingPublication findByIsbn(String isbn) {
        try (EntityManager em = emf.createEntityManager()) {
            List<PendingPublication> publications = em.createQuery("SELECT p FROM PendingPublication p WHERE p.isbn = :isbn", PendingPublication.class)
                    .setParameter("isbn", isbn)
                    .getResultList();
            return publications.isEmpty() ? null : publications.get(0);
        }
    }

    public List<PendingPublication> findAll() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT p FROM PendingPublication p", PendingPublication.class).getResultList();
        }
    }
}
