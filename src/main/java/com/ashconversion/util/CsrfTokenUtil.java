package com.ashconversion.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Gestion des tokens CSRF pour protéger les formulaires.
 */
@Component
public class CsrfTokenUtil {

    private static final Logger logger = LoggerFactory.getLogger(CsrfTokenUtil.class);

    private static final String CSRF_TOKEN_ATTRIBUTE = "csrfToken";
    private static final String CSRF_TOKEN_PARAM = "_csrf";
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final int TOKEN_LENGTH = 32;

    /**
     * Génère un token CSRF et le stocke dans la session
     */
    public static String generateToken(HttpSession session) {
        byte[] tokenBytes = new byte[TOKEN_LENGTH];
        secureRandom.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        session.setAttribute(CSRF_TOKEN_ATTRIBUTE, token);
        logger.debug("Token CSRF généré pour session: {}", session.getId());
        return token;
    }

    /**
     * Récupère le token CSRF ou en crée un nouveau
     */
    public static String getOrCreateToken(HttpSession session) {
        String token = (String) session.getAttribute(CSRF_TOKEN_ATTRIBUTE);
        if (token == null || token.isEmpty()) {
            token = generateToken(session);
        }
        return token;
    }

    /**
     * Vérifie que le token CSRF dans la requête correspond à celui de la session
     */
    public static boolean validateToken(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return false;
        String sessionToken = (String) session.getAttribute(CSRF_TOKEN_ATTRIBUTE);
        if (sessionToken == null) return false;

        String requestToken = request.getParameter(CSRF_TOKEN_PARAM);
        if (requestToken == null) requestToken = request.getHeader("X-CSRF-Token");
        if (requestToken == null) return false;

        return constantTimeEquals(sessionToken, requestToken);
    }

    /**
     * Comparaison constante pour éviter les attaques par timing
     */
    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) result |= a.charAt(i) ^ b.charAt(i);
        return result == 0;
    }

    public String getTokenParameterName() {
        return CSRF_TOKEN_PARAM;
    }
}
