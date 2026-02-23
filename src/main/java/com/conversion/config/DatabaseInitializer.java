package com.Ash_Conversion.config;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebListener
public class DatabaseInitializer implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);
    private static EntityManagerFactory emf;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            logger.info("Initialisation de la base de données MySQL...");
            emf = Persistence.createEntityManagerFactory("Ash_ConversionPU");

            logger.info("EntityManagerFactory initialisé avec succès");
        } catch (Exception e) {
            logger.error("❌ Échec critique : impossible d'initialiser la base de données", e);
            throw new RuntimeException("Impossible d'initialiser la base de données", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (emf != null && emf.isOpen()) {
            emf.close();
            logger.info("EntityManagerFactory fermé");
        }
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        if (emf == null) {
            throw new IllegalStateException("EntityManagerFactory non initialisé");
        }
        return emf;
    }
}


