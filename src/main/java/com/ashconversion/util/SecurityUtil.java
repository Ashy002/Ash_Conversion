package com.ashconversion.util;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utilitaire pour les vérifications de sécurité.
 * Centralise l'authentification et la validation des chemins.
 */
@Component
public class SecurityUtil {

    private static final Logger logger = LoggerFactory.getLogger(SecurityUtil.class);
    private static final String SESSION_USER_ID_ATTRIBUTE = "userId";

    /**
     * Récupère l'ID de l'utilisateur actuellement authentifié depuis la session.
     * 
     * @param request La requête HTTP
     * @return L'ID de l'utilisateur, ou null si non authentifié
     */
    public Long getCurrentUserId(HttpServletRequest request) {
        if (request == null) return null;

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
     */
    public Long requireAuthentication(HttpServletRequest request) {
        return getCurrentUserId(request);
    }

    /**
     * Valide un chemin de fichier contre les attaques de path traversal.
     * Vérifie que le chemin est normalisé et ne contient pas de "..".
     */
    public boolean validatePath(String filePath) {
        if (filePath == null || filePath.isEmpty()) return false;

        try {
            Path path = Paths.get(filePath);
            Path normalized = path.normalize();

            // Le chemin normalisé ne doit pas contenir ".."
            if (normalized.toString().contains("..")) {
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
     */
    public boolean validatePathInBase(String filePath, String allowedBasePath) {
        if (!validatePath(filePath) || allowedBasePath == null) return false;

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

