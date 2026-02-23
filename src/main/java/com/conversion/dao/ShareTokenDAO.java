package com.Ash_Conversion.dao;

import com.Ash_Conversion.config.DatabaseConfig;
import com.Ash_Conversion.model.entity.ShareToken;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DAO pour la gestion des ShareToken avec JPA.
 */
public class ShareTokenDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(ShareTokenDAO.class);
    
    /**
     * Crée un nouveau ShareToken.
     */
    public void create(ShareToken shareToken) {
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(shareToken);
            tx.commit();
            logger.debug("ShareToken créé: {}", shareToken.getToken());
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            logger.error("Erreur lors de la création du ShareToken", e);
            throw new RuntimeException("Erreur lors de la création du ShareToken", e);
        } finally {
            em.close();
        }
    }
    
    /**
     * Recherche un ShareToken par son token.
     */
    public ShareToken findByToken(String token) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            TypedQuery<ShareToken> query = em.createQuery(
                "SELECT st FROM ShareToken st WHERE st.token = :token", ShareToken.class);
            query.setParameter("token", token);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }
    
    /**
     * Met à jour un ShareToken.
     */
    public void update(ShareToken shareToken) {
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(shareToken);
            tx.commit();
            logger.debug("ShareToken mis à jour: {}", shareToken.getToken());
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            logger.error("Erreur lors de la mise à jour du ShareToken", e);
            throw new RuntimeException("Erreur lors de la mise à jour du ShareToken", e);
        } finally {
            em.close();
        }
    }
    
    /**
     * Supprime un ShareToken.
     */
    public void delete(ShareToken shareToken) {
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            ShareToken managedToken = em.merge(shareToken);
            em.remove(managedToken);
            tx.commit();
            logger.debug("ShareToken supprimé: {}", shareToken.getToken());
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            logger.error("Erreur lors de la suppression du ShareToken", e);
            throw new RuntimeException("Erreur lors de la suppression du ShareToken", e);
        } finally {
            em.close();
        }
    }
}
