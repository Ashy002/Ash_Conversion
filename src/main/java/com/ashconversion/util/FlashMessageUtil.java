package com.ashconversion.util;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

/**
 * Utilitaire Spring Boot pour gérer les messages flash (success / error) via la session.
 * Les messages sont affichés une seule fois puis supprimés.
 */
@Component
public class FlashMessageUtil {

    private static final String FLASH_SUCCESS = "flashSuccess";
    private static final String FLASH_ERROR = "flashError";

    /** Ajoute un message de succès dans la session */
    public static void addSuccess(HttpSession session, String message) {
        if (session != null) {
            session.setAttribute(FLASH_SUCCESS, message);
        }
    }

    /** Ajoute un message d'erreur dans la session */
    public static void addError(HttpSession session, String message) {
        if (session != null) {
            session.setAttribute(FLASH_ERROR, message);
        }
    }

    /** Récupère et supprime le message de succès */
    public String getAndRemoveSuccess(HttpSession session) {
        if (session == null) return null;
        String msg = (String) session.getAttribute(FLASH_SUCCESS);
        session.removeAttribute(FLASH_SUCCESS);
        return msg;
    }

    /** Récupère et supprime le message d'erreur */
    public String getAndRemoveError(HttpSession session) {
        if (session == null) return null;
        String msg = (String) session.getAttribute(FLASH_ERROR);
        session.removeAttribute(FLASH_ERROR);
        return msg;
    }
}

