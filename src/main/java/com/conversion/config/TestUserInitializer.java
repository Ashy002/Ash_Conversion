package com.Ash_Conversion.config;

import com.Ash_Conversion.config.DatabaseInitializer;
import com.Ash_Conversion.dao.UserDAO;
import com.Ash_Conversion.model.entity.User;
import com.Ash_Conversion.util.PasswordUtil;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

/**
 * Listener pour créer un utilisateur de test au démarrage de l'application.
 * Optionnel : peut être désactivé en production.
 */
@WebListener
public class TestUserInitializer implements ServletContextListener {
    
    private static final Logger logger = LoggerFactory.getLogger(TestUserInitializer.class);
    
    // Compte de test
    private static final String TEST_USERNAME = "ASHY";
    private static final String TEST_EMAIL = "ashy@gmail.com";
    private static final String TEST_PASSWORD = "ash1234";
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            logger.info("Initialisation de l'utilisateur de test...");
            
            // Attendre un peu pour s'assurer que DatabaseInitializer a terminé
            // (les listeners peuvent s'exécuter dans n'importe quel ordre)
            Thread.sleep(500);
            
            // Vérifier que l'EntityManagerFactory est initialisé
            if (DatabaseInitializer.getEntityManagerFactory() == null) {
                logger.warn("EntityManagerFactory non initialisé, attente supplémentaire...");
                Thread.sleep(1000);
                if (DatabaseInitializer.getEntityManagerFactory() == null) {
                    logger.error("Impossible d'initialiser l'utilisateur de test: EntityManagerFactory non disponible");
                    return;
                }
            }
            
            UserDAO userDAO = new UserDAO();
            
            // Vérifier si l'utilisateur existe déjà
            User existingUser = userDAO.findByUsername(TEST_USERNAME);
            
            if (existingUser == null) {
                // Créer l'utilisateur de test
                User testUser = new User();
                testUser.setUsername(TEST_USERNAME);
                testUser.setEmail(TEST_EMAIL);
                testUser.setPasswordHash(PasswordUtil.hashPassword(TEST_PASSWORD));
                testUser.setCreatedAt(LocalDateTime.now());
                testUser.setIsActive(true);
                
                userDAO.create(testUser);
                
                logger.info("Utilisateur de test créé avec succès:");
                logger.info("  Username: {}", TEST_USERNAME);
                logger.info("  Email: {}", TEST_EMAIL);
                logger.info("  Password: {}", TEST_PASSWORD);
            } else {
                logger.info("Utilisateur de test existe déjà: {}", TEST_USERNAME);
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Interruption lors de l'initialisation de l'utilisateur de test", e);
        } catch (Exception e) {
            logger.error("Erreur lors de la création de l'utilisateur de test", e);
            logger.error("Type d'erreur: {}", e.getClass().getName());
            logger.error("Message: {}", e.getMessage());
            if (e.getCause() != null) {
                logger.error("Cause: {}", e.getCause().getMessage());
            }
            // Ne pas bloquer le démarrage de l'application en cas d'erreur
        }
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Nettoyage si nécessaire
    }
}

