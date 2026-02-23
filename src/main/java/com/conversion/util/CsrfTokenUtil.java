package com.Ash_Conversion.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utilitaire pour la gestion des tokens CSRF.
 * Protège contre les attaques Cross-Site Request Forgery.
 */
public class CsrfTokenUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(CsrfTokenUtil.class);
    private static final String CSRF_TOKEN_ATTRIBUTE = "csrfToken";
    private static final String CSRF_TOKEN_PARAM = "_csrf";
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final int TOKEN_LENGTH = 32;
    
    private CsrfTokenUtil() {
        // Classe utilitaire
    }
    
    /**
     * Génère un nouveau token CSRF et le stocke dans la session.
     * 
     * @param session La session HTTP
     * @return Le token CSRF généré
     */
    public static String generateToken(HttpSession session) {
        if (session == null) {
            return null;
        }
        
        byte[] tokenBytes = new byte[TOKEN_LENGTH];
        secureRandom.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        
        session.setAttribute(CSRF_TOKEN_ATTRIBUTE, token);
        logger.debug("Token CSRF généré pour la session: {}", session.getId());
        
        return token;
    }
    
    /**
     * Récupère le token CSRF de la session, ou en génère un nouveau s'il n'existe pas.
     * 
     * @param session La session HTTP
     * @return Le token CSRF
     */
    public static String getOrCreateToken(HttpSession session) {
        if (session == null) {
            return null;
        }
        
        String token = (String) session.getAttribute(CSRF_TOKEN_ATTRIBUTE);
        if (token == null || token.isEmpty()) {
            token = generateToken(session);
        }
        
        return token;
    }
    
    /**
     * Vérifie si le token CSRF fourni dans la requête correspond à celui de la session.
     * 
     * @param request La requête HTTP
     * @return true si le token est valide, false sinon
     */
    public static boolean validateToken(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            logger.warn("Tentative de validation CSRF sans session");
            return false;
        }
        
        String sessionToken = (String) session.getAttribute(CSRF_TOKEN_ATTRIBUTE);
        if (sessionToken == null || sessionToken.isEmpty()) {
            logger.warn("Token CSRF manquant dans la session");
            return false;
        }
        
        // Récupérer le token depuis le paramètre ou l'en-tête
        String requestToken = request.getParameter(CSRF_TOKEN_PARAM);
        if (requestToken == null) {
            requestToken = request.getHeader("X-CSRF-Token");
        }
        
        if (requestToken == null || requestToken.isEmpty()) {
            logger.warn("Token CSRF manquant dans la requête");
            return false;
        }
        
        // Comparaison constante dans le temps pour éviter les attaques par timing
        boolean isValid = constantTimeEquals(sessionToken, requestToken);
        
        if (!isValid) {
            logger.warn("Token CSRF invalide pour la session: {}", session.getId());
        }
        
        return isValid;
    }
    
    /**
     * Compare deux chaînes de manière constante dans le temps pour éviter les attaques par timing.
     * 
     * @param a Première chaîne
     * @param b Deuxième chaîne
     * @return true si les chaînes sont égales
     */
    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        
        if (a.length() != b.length()) {
            return false;
        }
        
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        
        return result == 0;
    }
    
    /**
     * Retourne le nom du paramètre CSRF à utiliser dans les formulaires.
     * 
     * @return Le nom du paramètre
     */
    public static String getTokenParameterName() {
        return CSRF_TOKEN_PARAM;
    }
}
