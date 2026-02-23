package com.Ash_Conversion.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilitaire pour assainir les noms de fichiers dans les en-têtes HTTP.
 * Protège contre les injections dans Content-Disposition.
 */
public class FilenameSanitizer {
    
    private static final Logger logger = LoggerFactory.getLogger(FilenameSanitizer.class);
    
    private FilenameSanitizer() {
        // Classe utilitaire
    }
    
    /**
     * Assainit un nom de fichier pour l'utiliser dans l'en-tête Content-Disposition.
     * Remplace les caractères dangereux et limite la longueur.
     * 
     * @param filename Le nom de fichier original
     * @return Le nom de fichier assaini
     */
    public static String sanitizeForContentDisposition(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "file";
        }
        
        // Remplacer les caractères dangereux pour les en-têtes HTTP
        // Caractères à éviter : \r, \n, ", \, et autres caractères de contrôle
        String sanitized = filename
            .replace("\r", "")
            .replace("\n", "")
            .replace("\"", "'")
            .replace("\\", "_")
            .replaceAll("[\\x00-\\x1F\\x7F]", "_"); // Caractères de contrôle
        
        // Limiter la longueur (RFC 2183 recommande max 255 octets)
        if (sanitized.length() > 200) {
            int lastDot = sanitized.lastIndexOf('.');
            if (lastDot > 0) {
                String ext = sanitized.substring(lastDot);
                String name = sanitized.substring(0, lastDot);
                sanitized = name.substring(0, Math.min(200 - ext.length(), name.length())) + ext;
            } else {
                sanitized = sanitized.substring(0, 200);
            }
        }
        
        return sanitized;
    }
}

