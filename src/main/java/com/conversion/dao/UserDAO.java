package com.Ash_Conversion.dao;

import com.Ash_Conversion.model.entity.User;
import com.Ash_Conversion.config.DatabaseConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * DAO pour la gestion des utilisateurs avec JPA.
 * Utilise EntityManager pour les opérations CRUD.
 */
public class UserDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);
    
    /**
     * Crée un nouvel utilisateur dans la base de données.
     * 
     * @param user L'utilisateur à créer
     * @return L'utilisateur créé avec son ID généré
     */
    public User create(User user) {
        EntityManager em = null;
        EntityTransaction transaction = null;
        
        try {
            // Vérifier que l'EntityManagerFactory est initialisé
            try {
                em = DatabaseConfig.getEntityManager();
            } catch (IllegalStateException e) {
                logger.error("EntityManagerFactory non initialisé. La base de données n'est pas prête.");
                throw new IllegalStateException("La base de données n'est pas initialisée. Veuillez redémarrer l'application.", e);
            }
            transaction = em.getTransaction();
            
            logger.info("Début de la création de l'utilisateur: {}", user.getUsername());
            transaction.begin();
            
            // Vérifier que les champs requis ne sont pas null
            if (user.getUsername() == null || user.getEmail() == null || user.getPasswordHash() == null) {
                transaction.rollback();
                logger.error("Tentative de création d'utilisateur avec champs null: username={}, email={}, passwordHash={}", 
                            user.getUsername(), user.getEmail(), user.getPasswordHash() != null ? "***" : null);
                throw new IllegalArgumentException("Les champs username, email et passwordHash sont requis");
            }
            
            em.persist(user);
            em.flush(); // Forcer l'écriture immédiate pour détecter les erreurs de contrainte avant commit
            transaction.commit();
            
            if (user.getId() == null) {
                logger.error("L'utilisateur a été persisté mais l'ID n'a pas été généré: {}", user.getUsername());
                throw new RuntimeException("L'ID de l'utilisateur n'a pas été généré après persist");
            }
            
            logger.info("Utilisateur créé avec succès: {} (ID: {})", user.getUsername(), user.getId());
            return user;
            
        } catch (jakarta.persistence.PersistenceException e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            
            // Vérifier si c'est une violation de contrainte unique
            Throwable cause = e.getCause();
            if (cause != null && (cause.getMessage() != null && 
                (cause.getMessage().contains("unique") || 
                 cause.getMessage().contains("duplicate") ||
                 cause.getMessage().contains("constraint")))) {
                logger.warn("Violation de contrainte unique lors de la création de l'utilisateur: {}", user.getUsername());
                throw new RuntimeException("Un utilisateur avec ce nom d'utilisateur ou cet email existe déjà", e);
            }
            
            logger.error("Erreur de persistance lors de la création de l'utilisateur: {}", user.getUsername(), e);
            logger.error("Type d'erreur: {}", e.getClass().getName());
            logger.error("Message: {}", e.getMessage());
            if (cause != null) {
                logger.error("Cause: {} - {}", cause.getClass().getName(), cause.getMessage());
            }
            throw new RuntimeException("Erreur lors de la création de l'utilisateur: " + e.getMessage(), e);
            
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("Erreur inattendue lors de la création de l'utilisateur: {}", user.getUsername(), e);
            logger.error("Type d'erreur: {}", e.getClass().getName());
            logger.error("Message: {}", e.getMessage());
            if (e.getCause() != null) {
                logger.error("Cause: {} - {}", e.getCause().getClass().getName(), e.getCause().getMessage());
            }
            throw new RuntimeException("Erreur lors de la création de l'utilisateur: " + e.getMessage(), e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
    
    /**
     * Trouve un utilisateur par son ID.
     * 
     * @param id L'ID de l'utilisateur
     * @return L'utilisateur trouvé ou null
     */
    public User findById(Long id) {
        EntityManager em = null;
        try {
            em = DatabaseConfig.getEntityManager();
            User user = em.find(User.class, id);
            if (user != null) {
                logger.debug("Utilisateur trouvé par ID: {}", id);
            }
            return user;
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche de l'utilisateur par ID: {}", id, e);
            return null;
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
    
    /**
     * Trouve un utilisateur par son nom d'utilisateur.
     * 
     * @param username Le nom d'utilisateur
     * @return L'utilisateur trouvé ou null
     */
    public User findByUsername(String username) {
        EntityManager em = null;
        try {
            em = DatabaseConfig.getEntityManager();
            TypedQuery<User> query = em.createQuery(
                "SELECT u FROM User u WHERE u.username = :username", User.class);
            query.setParameter("username", username);
            User user = query.getSingleResult();
            logger.debug("Utilisateur trouvé par username: {}", username);
            return user;
        } catch (NoResultException e) {
            logger.debug("Aucun utilisateur trouvé avec le username: {}", username);
            return null;
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche de l'utilisateur par username: {}", username, e);
            return null;
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
    
    /**
     * Trouve un utilisateur par son email.
     * 
     * @param email L'email de l'utilisateur
     * @return L'utilisateur trouvé ou null
     */
    public User findByEmail(String email) {
        EntityManager em = null;
        try {
            em = DatabaseConfig.getEntityManager();
            TypedQuery<User> query = em.createQuery(
                "SELECT u FROM User u WHERE u.email = :email", User.class);
            query.setParameter("email", email);
            User user = query.getSingleResult();
            logger.debug("Utilisateur trouvé par email: {}", email);
            return user;
        } catch (NoResultException e) {
            logger.debug("Aucun utilisateur trouvé avec l'email: {}", email);
            return null;
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche de l'utilisateur par email: {}", email, e);
            return null;
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
    
    /**
     * Met à jour un utilisateur existant.
     * 
     * @param user L'utilisateur à mettre à jour
     */
    public void update(User user) {
        EntityManager em = null;
        EntityTransaction transaction = null;
        
        try {
            em = DatabaseConfig.getEntityManager();
            transaction = em.getTransaction();
            
            transaction.begin();
            em.merge(user);
            transaction.commit();
            logger.debug("Utilisateur mis à jour: {}", user.getUsername());
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("Erreur lors de la mise à jour de l'utilisateur: {}", user.getUsername(), e);
            throw new RuntimeException("Erreur lors de la mise à jour de l'utilisateur", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
    
    /**
     * Met à jour la date de dernière connexion.
     * 
     * @param user L'utilisateur
     */
    public void updateLastLogin(User user) {
        EntityManager em = null;
        EntityTransaction transaction = null;
        
        try {
            em = DatabaseConfig.getEntityManager();
            transaction = em.getTransaction();
            
            transaction.begin();
            user.setLastLogin(LocalDateTime.now());
            em.merge(user);
            transaction.commit();
            logger.debug("Date de dernière connexion mise à jour pour: {}", user.getUsername());
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("Erreur lors de la mise à jour de la dernière connexion", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
}
