package io.wahid.publication.repository;

import io.wahid.publication.model.Magazine;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;

public class MagazineRepository {

    private final EntityManagerFactory emf;

    public MagazineRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public Long count() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT COUNT(m) FROM Magazine m", Long.class).getSingleResult();
        }
    }

    public List<Magazine> findMagazines(int offset, int limit) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT m FROM Magazine m", Magazine.class)
                    .setFirstResult(offset)
                    .setMaxResults(limit)
                    .getResultList();
        }
    }
}