package com.Ash_Conversion.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

/**
 * Utilitaire pour charger et accéder aux propriétés de configuration.
 * Utilise le logging au lieu de printStackTrace pour la sécurité.
 */
public class ConfigUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigUtil.class);
    private static Properties properties;
    
    static {
        try {
            properties = new Properties();
            InputStream inputStream = ConfigUtil.class.getClassLoader()
                    .getResourceAsStream("config.properties");
            if (inputStream != null) {
                properties.load(inputStream);
                logger.info("Configuration chargée avec succès");
            } else {
                logger.warn("Fichier config.properties non trouvé");
            }
        } catch (Exception e) {
            logger.error("Erreur lors du chargement de la configuration", e);
            properties = new Properties(); // Initialiser avec Properties vide pour éviter NPE
        }
    }
    
    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    public static int getIntProperty(String key, int defaultValue) {
        String value = getProperty(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                logger.warn("Valeur invalide pour la propriété {}: {}", key, value);
            }
        }
        return defaultValue;
    }
    
    public static long getLongProperty(String key, long defaultValue) {
        String value = getProperty(key);
        if (value != null) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                logger.warn("Valeur invalide pour la propriété {}: {}", key, value);
            }
        }
        return defaultValue;
    }
}

