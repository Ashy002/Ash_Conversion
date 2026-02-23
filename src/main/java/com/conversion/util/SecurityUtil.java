package com.Ash_Conversion.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utilitaire pour les vérifications de sécurité.
 * Centralise l'authentification et la validation des chemins.
 */
public class SecurityUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityUtil.class);
    private static final String SESSION_USER_ID_ATTRIBUTE = "userId";
    
    private SecurityUtil() {
        // Classe utilitaire, pas d'instanciation
    }
    
    /**
     * Récupère l'ID de l'utilisateur actuellement authentifié depuis la session.
     * 
     * @param request La requête HTTP
     * @return L'ID de l'utilisateur, ou null si non authentifié
     */
    public static Long getCurrentUserId(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        
        Object userId = request.getSession(false) != null 
            ? request.getSession().getAttribute(SESSION_USER_ID_ATTRIBUTE)
            : null;
        
        if (userId instanceof Long) {
            return (Long) userId;
        }
        
        return null;
    }
    
    /**
     * Vérifie que l'utilisateur est authentifié.
     * Retourne l'ID de l'utilisateur si authentifié, null sinon.
     * 
     * @param request La requête HTTP
     * @return L'ID de l'utilisateur si authentifié, null sinon
     */
    public static Long requireAuthentication(HttpServletRequest request) {
        return getCurrentUserId(request);
    }
    
    /**
     * Valide un chemin de fichier contre les attaques de path traversal.
     * Vérifie que le chemin est normalisé et ne contient pas de "..".
     * 
     * @param filePath Le chemin du fichier à valider
     * @return true si le chemin est valide, false sinon
     */
    public static boolean validatePath(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }
        
        try {
            Path path = Paths.get(filePath);
            Path normalized = path.normalize();
            
            // Vérifier qu'il n'y a pas de path traversal
            // Le chemin normalisé ne doit pas contenir ".."
            String normalizedStr = normalized.toString();
            if (normalizedStr.contains("..")) {
                logger.warn("Tentative de path traversal détectée: {}", filePath);
                return false;
            }
            
            return true;
        } catch (Exception e) {
            logger.warn("Erreur lors de la validation du chemin: {}", filePath, e);
            return false;
        }
    }
    
    /**
     * Valide qu'un chemin de fichier est dans un répertoire autorisé.
     * 
     * @param filePath Le chemin du fichier
     * @param allowedBasePath Le répertoire de base autorisé
     * @return true si le fichier est dans le répertoire autorisé
     */
    public static boolean validatePathInBase(String filePath, String allowedBasePath) {
        if (!validatePath(filePath) || allowedBasePath == null) {
            return false;
        }
        
        try {
            Path file = Paths.get(filePath).normalize();
            Path base = Paths.get(allowedBasePath).normalize();
            
            return file.startsWith(base);
        } catch (Exception e) {
            logger.warn("Erreur lors de la validation du chemin dans la base: {}", filePath, e);
            return false;
        }
    }
}

