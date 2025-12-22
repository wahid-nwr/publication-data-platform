package io.wahid.publication.repository;

import io.wahid.publication.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

public class DBSanitizer {
    private final EntityManagerFactory emf = JpaUtil.getEntityManagerFactory();

    public DBSanitizer() {}

    public void sanitizeDB() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();

            // Example: Delete all instances of a 'Product' entity
            int deletedCount = em.createQuery("DELETE FROM PendingPublication").executeUpdate();
            System.out.println("Deleted " + deletedCount + " PendingPublication.");

            deletedCount = em.createQuery("DELETE FROM PublicationModel").executeUpdate();
            System.out.println("Deleted " + deletedCount + " Publication.");

            deletedCount = em.createQuery("DELETE FROM Author").executeUpdate();
            System.out.println("Deleted " + deletedCount + " Author.");

            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }
}
