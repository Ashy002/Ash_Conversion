package com.Ash_Conversion.util;

import jakarta.servlet.http.HttpSession;

/**
 * Utilitaire pour gérer les messages flash (success/error) via la session.
 * Les messages flash sont affichés une seule fois puis supprimés.
 */
public class FlashMessageUtil {
    
    private static final String FLASH_SUCCESS = "flashSuccess";
    private static final String FLASH_ERROR = "flashError";
    
    /**
     * Ajoute un message de succès dans la session.
     * 
     * @param session La session HTTP
     * @param message Le message de succès
     */
    public static void addSuccess(HttpSession session, String message) {
        if (session != null) {
            session.setAttribute(FLASH_SUCCESS, message);
        }
    }
    
    /**
     * Ajoute un message d'erreur dans la session.
     * 
     * @param session La session HTTP
     * @param message Le message d'erreur
     */
    public static void addError(HttpSession session, String message) {
        if (session != null) {
            session.setAttribute(FLASH_ERROR, message);
        }
    }
    
    /**
     * Récupère et supprime le message de succès de la session.
     * 
     * @param session La session HTTP
     * @return Le message de succès ou null
     */
    public static String getAndRemoveSuccess(HttpSession session) {
        if (session == null) {
            return null;
        }
        String message = (String) session.getAttribute(FLASH_SUCCESS);
        session.removeAttribute(FLASH_SUCCESS);
        return message;
    }
    
    /**
     * Récupère et supprime le message d'erreur de la session.
     * 
     * @param session La session HTTP
     * @return Le message d'erreur ou null
     */
    public static String getAndRemoveError(HttpSession session) {
        if (session == null) {
            return null;
        }
        String message = (String) session.getAttribute(FLASH_ERROR);
        session.removeAttribute(FLASH_ERROR);
        return message;
    }
}

