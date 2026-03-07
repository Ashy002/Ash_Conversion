package com.Ash_Conversion.config;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@WebListener
public class DatabaseInitializer implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);
    private static EntityManagerFactory emf;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            logger.info("Initialisation de la base de données MySQL...");

            String host     = System.getenv("MYSQLHOST");
            String port     = System.getenv("MYSQLPORT");
            String database = System.getenv("MYSQLDATABASE");
            String user     = System.getenv("MYSQLUSER");
            String password = System.getenv("MYSQLPASSWORD");

            // Log pour debug (sans le mot de passe)
            logger.info("DB host={}, port={}, database={}, user={}", host, port, database, user);

            if (host == null || port == null || database == null) {
                throw new RuntimeException(
                    "Variables d'environnement MySQL manquantes ! " +
                    "Vérifiez MYSQLHOST, MYSQLPORT, MYSQLDATABASE dans Railway."
                );
            }

            String url = "jdbc:mysql://" + host + ":" + port + "/" + database
                       + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

            Map<String, String> props = new HashMap<>();
            props.put("jakarta.persistence.jdbc.url",      url);
            props.put("jakarta.persistence.jdbc.user",     user);
            props.put("jakarta.persistence.jdbc.password", password);

            emf = Persistence.createEntityManagerFactory("Ash_ConversionPU", props);

            logger.info("✅ EntityManagerFactory initialisé avec succès");
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