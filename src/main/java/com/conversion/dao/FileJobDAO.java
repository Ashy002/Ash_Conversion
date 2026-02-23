package com.Ash_Conversion.dao;

import com.Ash_Conversion.config.DatabaseConfig;
import com.Ash_Conversion.model.entity.FileJob;
import com.Ash_Conversion.model.entity.User;
import com.Ash_Conversion.model.enums.ConversionStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * DAO pour la gestion des FileJob avec JPA.
 * Fournit des méthodes de recherche, filtrage et pagination.
 */
public class FileJobDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(FileJobDAO.class);
    
    /**
     * Crée un nouveau FileJob.
     */
    public FileJob create(FileJob fileJob) {
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        
        try {
            transaction.begin();
            em.persist(fileJob);
            transaction.commit();
            logger.debug("FileJob créé: {}", fileJob.getOriginalFilename());
            return fileJob;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("Erreur lors de la création du FileJob", e);
            throw new RuntimeException("Erreur lors de la création du FileJob", e);
        } finally {
            em.close();
        }
    }
    
    /**
     * Trouve un FileJob par son ID.
     */
    public FileJob findById(Long id) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            FileJob fileJob = em.find(FileJob.class, id);
            return fileJob;
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche du FileJob par ID: {}", id, e);
            return null;
        } finally {
            em.close();
        }
    }
    
    /**
     * Trouve tous les FileJob d'un utilisateur.
     */
    public List<FileJob> findByUser(User user) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            TypedQuery<FileJob> query = em.createQuery(
                "SELECT fj FROM FileJob fj WHERE fj.user = :user ORDER BY fj.createdAt DESC", 
                FileJob.class);
            query.setParameter("user", user);
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche des FileJob par utilisateur", e);
            return List.of();
        } finally {
            em.close();
        }
    }
    
    /**
     * Trouve les FileJob d'un utilisateur avec un statut spécifique.
     */
    public List<FileJob> findByUserAndStatus(User user, ConversionStatus status) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            TypedQuery<FileJob> query = em.createQuery(
                "SELECT fj FROM FileJob fj WHERE fj.user = :user AND fj.status = :status ORDER BY fj.createdAt DESC", 
                FileJob.class);
            query.setParameter("user", user);
            query.setParameter("status", status);
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche des FileJob par utilisateur et statut", e);
            return List.of();
        } finally {
            em.close();
        }
    }
    
    /**
     * Recherche les FileJob d'un utilisateur par nom de fichier (recherche partielle).
     */
    public List<FileJob> findByUserAndFilename(User user, String searchTerm) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            TypedQuery<FileJob> query = em.createQuery(
                "SELECT fj FROM FileJob fj WHERE fj.user = :user AND " +
                "LOWER(fj.originalFilename) LIKE LOWER(:searchTerm) ORDER BY fj.createdAt DESC", 
                FileJob.class);
            query.setParameter("user", user);
            query.setParameter("searchTerm", "%" + searchTerm + "%");
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche des FileJob par nom", e);
            return List.of();
        } finally {
            em.close();
        }
    }
    
    /**
     * Recherche paginée des FileJob d'un utilisateur avec filtres optionnels.
     * 
     * @param user L'utilisateur
     * @param status Le statut (null pour tous)
     * @param searchTerm Le terme de recherche (null pour tous)
     * @param page Le numéro de page (0-based)
     * @param pageSize La taille de la page
     * @return La liste des FileJob
     */
    public List<FileJob> findByUserWithFilters(User user, ConversionStatus status, 
                                               String searchTerm, int page, int pageSize) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                "SELECT fj FROM FileJob fj WHERE fj.user = :user");
            
            if (status != null) {
                jpql.append(" AND fj.status = :status");
            }
            
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                jpql.append(" AND LOWER(fj.originalFilename) LIKE LOWER(:searchTerm)");
            }
            
            jpql.append(" ORDER BY fj.createdAt DESC");
            
            TypedQuery<FileJob> query = em.createQuery(jpql.toString(), FileJob.class);
            query.setParameter("user", user);
            
            if (status != null) {
                query.setParameter("status", status);
            }
            
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                query.setParameter("searchTerm", "%" + searchTerm.trim() + "%");
            }
            
            query.setFirstResult(page * pageSize);
            query.setMaxResults(pageSize);
            
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche paginée des FileJob", e);
            return List.of();
        } finally {
            em.close();
        }
    }
    
    /**
     * Compte le nombre total de FileJob d'un utilisateur avec filtres optionnels.
     */
    public long countByUserWithFilters(User user, ConversionStatus status, String searchTerm) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                "SELECT COUNT(fj) FROM FileJob fj WHERE fj.user = :user");
            
            if (status != null) {
                jpql.append(" AND fj.status = :status");
            }
            
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                jpql.append(" AND LOWER(fj.originalFilename) LIKE LOWER(:searchTerm)");
            }
            
            TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);
            query.setParameter("user", user);
            
            if (status != null) {
                query.setParameter("status", status);
            }
            
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                query.setParameter("searchTerm", "%" + searchTerm.trim() + "%");
            }
            
            return query.getSingleResult();
        } catch (Exception e) {
            logger.error("Erreur lors du comptage des FileJob", e);
            return 0;
        } finally {
            em.close();
        }
    }
    
    /**
     * Compte les FileJob par statut pour un utilisateur.
     */
    public long countByUserAndStatus(User user, ConversionStatus status) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(fj) FROM FileJob fj WHERE fj.user = :user AND fj.status = :status", 
                Long.class);
            query.setParameter("user", user);
            query.setParameter("status", status);
            return query.getSingleResult();
        } catch (Exception e) {
            logger.error("Erreur lors du comptage des FileJob par statut", e);
            return 0;
        } finally {
            em.close();
        }
    }
    
    /**
     * Met à jour un FileJob.
     */
    public void update(FileJob fileJob) {
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        
        try {
            transaction.begin();
            em.merge(fileJob);
            transaction.commit();
            logger.debug("FileJob mis à jour: {}", fileJob.getId());
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("Erreur lors de la mise à jour du FileJob", e);
            throw new RuntimeException("Erreur lors de la mise à jour du FileJob", e);
        } finally {
            em.close();
        }
    }
    
    /**
     * Supprime un FileJob.
     */
    public void delete(FileJob fileJob) {
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        
        try {
            transaction.begin();
            FileJob managedFileJob = em.find(FileJob.class, fileJob.getId());
            if (managedFileJob != null) {
                em.remove(managedFileJob);
            }
            transaction.commit();
            logger.debug("FileJob supprimé: {}", fileJob.getId());
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("Erreur lors de la suppression du FileJob", e);
            throw new RuntimeException("Erreur lors de la suppression du FileJob", e);
        } finally {
            em.close();
        }
    }
}
