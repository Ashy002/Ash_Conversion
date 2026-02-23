package com.Ash_Conversion.filter;

import com.Ash_Conversion.constants.RouteConstants;
import com.Ash_Conversion.util.JsonResponseUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Filtre d'authentification pour protéger les routes nécessitant une session utilisateur.
 * Protège :
 * - /dashboard et toutes les pages JSP protégées
 * - /api/files/* (upload, list, delete)
 * - /api/convert/* (conversion)
 * - /api/preview/* (preview)
 * - /api/download/* (download)
 */
public class AuthenticationFilter implements Filter {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);
    private static final String SESSION_USER_ID_ATTRIBUTE = "userId";
    
    // Routes protégées (doivent commencer par ces chemins)
    private static final List<String> PROTECTED_PATHS = Arrays.asList(
        RouteConstants.DASHBOARD,
        "/api/files",
        "/api/convert",
        "/api/preview",
        "/api/download"
    );
    
    // Routes publiques (toujours accessibles)
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
        RouteConstants.LOGIN,
        RouteConstants.REGISTER,
        RouteConstants.SHARE_PUBLIC  // Partage peut être public (avec token)
    );
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("AuthenticationFilter initialisé");
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String requestPath = httpRequest.getServletPath();
        String pathInfo = httpRequest.getPathInfo();
        String fullPath = requestPath + (pathInfo != null ? pathInfo : "");
        
        // Vérifier si la route est publique
        if (isPublicPath(fullPath)) {
            chain.doFilter(request, response);
            return;
        }
        
        // Vérifier si la route est protégée
        if (isProtectedPath(fullPath)) {
            HttpSession session = httpRequest.getSession(false);
            
            // Vérifier si l'utilisateur est authentifié
            if (session == null || session.getAttribute(SESSION_USER_ID_ATTRIBUTE) == null) {
                handleUnauthorizedAccess(httpRequest, httpResponse, fullPath);
                return;
            }
        }
        
        // Route non protégée ou utilisateur authentifié, continuer
        chain.doFilter(request, response);
    }
    
    /**
     * Vérifie si un chemin est public (accessible sans authentification).
     */
    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }
    
    /**
     * Vérifie si un chemin est protégé (nécessite authentification).
     */
    private boolean isProtectedPath(String path) {
        return PROTECTED_PATHS.stream().anyMatch(path::startsWith);
    }
    
    /**
     * Gère l'accès non autorisé selon le type de requête (API ou page web).
     */
    private void handleUnauthorizedAccess(HttpServletRequest request, 
                                         HttpServletResponse response, 
                                         String path) throws IOException {
        // Si c'est une requête API, retourner 401 JSON
        if (path.startsWith("/api")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonResponseUtil.sendError(response, "Authentification requise", 
                                     HttpServletResponse.SC_UNAUTHORIZED);
            logger.warn("Tentative d'accès non autorisé à l'API: {}", path);
        } else {
            // Sinon, rediriger vers login
            String loginUrl = request.getContextPath() + RouteConstants.LOGIN;
            response.sendRedirect(loginUrl);
            logger.debug("Redirection vers login pour: {}", path);
        }
    }
    
    @Override
    public void destroy() {
        logger.info("AuthenticationFilter détruit");
    }
}

