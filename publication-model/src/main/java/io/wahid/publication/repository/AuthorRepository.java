package io.wahid.publication.repository;

import io.wahid.publication.model.Author;
import jakarta.persistence.*;

import java.util.List;

public class AuthorRepository {
    private final EntityManagerFactory emf;

    public AuthorRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public void save(Author author) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(author);
            em.getTransaction().commit();
        }
    }

    public boolean doesExist(String email) {
        return !findByEmail(List.of(email)).isEmpty();
    }

    public List<Author> findByEmail(List<String> emails) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT au FROM Author au WHERE au.email IN :emails", Author.class)
                    .setParameter("emails", emails)
                    .getResultList();
        }
    }

    public Long count() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT COUNT(au) FROM Author au", Long.class).getSingleResult();
        }
    }

    public List<Author> findAuthors(Integer offset, Integer limit) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT au FROM Author au", Author.class)
                    .setFirstResult(offset)
                    .setMaxResults(limit)
                    .getResultList();
        }
    }

    public Author findById(Long id) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT a FROM Author a WHERE a.id = :id", Author.class)
                    .setParameter("id", id)
                    .getSingleResult();
        }
    }

    public boolean existsAll(List<String> authorEmails) {
        return authorEmails.size() == findByEmail(authorEmails).size();
    }
}