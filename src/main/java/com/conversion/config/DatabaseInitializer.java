package com.conversion.config;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@WebListener
public class DatabaseInitializer implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);
    private static EntityManagerFactory emf;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            logger.info("Initialisation de la base de données...");

            // 1. Essayer avec les variables d'environnement (hébergement)
            String host     = System.getenv("MYSQLHOST");
            String port     = System.getenv("MYSQLPORT");
            String database = System.getenv("MYSQLDATABASE");
            String user     = System.getenv("MYSQLUSER");
            String password = System.getenv("MYSQLPASSWORD");

            Map<String, String> props = new HashMap<>();

            if (host != null && port != null && database != null) {
                logger.info("Utilisation des variables d'environnement MySQL");
                String url = "jdbc:mysql://" + host + ":" + port + "/" + database
                           + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

                props.put("jakarta.persistence.jdbc.url", url);
                props.put("jakarta.persistence.jdbc.user", user);
                props.put("jakarta.persistence.jdbc.password", password);

            } else {
                // 2. Sinon, fallback sur db.properties (local)
                logger.info("Variables MySQL absentes, lecture de db.properties");
                try (InputStream in = getClass().getClassLoader().getResourceAsStream("db.properties")) {
                    if (in == null) {
                        throw new RuntimeException("Fichier db.properties introuvable !");
                    }
                    Properties fileProps = new Properties();
                    fileProps.load(in);

                    props.put("jakarta.persistence.jdbc.url", fileProps.getProperty("db.url"));
                    props.put("jakarta.persistence.jdbc.user", fileProps.getProperty("db.user"));
                    props.put("jakarta.persistence.jdbc.password", fileProps.getProperty("db.password"));
                }
            }

            // 3. Initialiser l’EntityManagerFactory
            emf = Persistence.createEntityManagerFactory("Ash_ConversionPU", props);
            logger.info("✅ EntityManagerFactory initialisé avec succès");

        } catch (Exception e) {
            logger.error("❌ Échec critique : impossible d'initialiser la base de données", e);
            throw new RuntimeException("Impossible d'initialiser la base de données", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (emf != null) {
            emf.close();
            logger.info("EntityManagerFactory fermé proprement");
        }
    }

    /**
     * Retourne l'EntityManagerFactory initialisé
     */
    public static EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }
}
