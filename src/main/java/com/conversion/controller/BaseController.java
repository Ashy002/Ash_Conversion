package com.Ash_Conversion.controller;

import com.Ash_Conversion.constants.RouteConstants;
import com.Ash_Conversion.constants.ViewConstants;
import com.Ash_Conversion.util.CsrfTokenUtil;
import com.Ash_Conversion.util.FlashMessageUtil;
import com.Ash_Conversion.util.JsonResponseUtil;
import com.Ash_Conversion.util.SecurityUtil;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Classe de base pour tous les controllers.
 * Fournit des méthodes utilitaires communes pour éviter la duplication.
 */
public abstract class BaseController extends HttpServlet {
    
    protected static final Logger logger = LoggerFactory.getLogger(BaseController.class);
    
    /**
     * Redirige vers une URL.
     */
    protected void redirect(HttpServletResponse response, String url) throws IOException {
        response.sendRedirect(response.encodeRedirectURL(url));
    }
    
    /**
     * Redirige vers une URL avec le contexte de l'application.
     */
    protected void redirectWithContext(HttpServletRequest request, HttpServletResponse response, String path) 
            throws IOException {
        redirect(response, request.getContextPath() + path);
    }
    
    /**
     * Forward vers une vue JSP.
     */
    protected void forwardToView(HttpServletRequest request, HttpServletResponse response, String viewPath) 
            throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath);
        dispatcher.forward(request, response);
    }
    
    /**
     * Forward vers la vue de login.
     */
    protected void forwardToLogin(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        forwardToView(request, response, ViewConstants.LOGIN_VIEW);
    }
    
    /**
     * Forward vers la vue de dashboard.
     */
    protected void forwardToDashboard(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        forwardToView(request, response, ViewConstants.DASHBOARD_VIEW);
    }
    
    /**
     * Forward vers la vue d'erreur.
     */
    protected void forwardToError(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        forwardToView(request, response, ViewConstants.ERROR_VIEW);
    }
    
    /**
     * Gère les erreurs de manière centralisée.
     * Ne expose pas les détails techniques à l'utilisateur.
     */
    protected void handleError(HttpServletRequest request, HttpServletResponse response, 
                               Exception e, String userMessage) throws ServletException, IOException {
        // Logger l'erreur complète côté serveur
        logger.error("Erreur dans {}: {}", getClass().getSimpleName(), e.getMessage(), e);
        
        // Ne pas exposer les détails techniques à l'utilisateur
        String safeMessage = userMessage != null ? userMessage : 
            "Une erreur est survenue. Veuillez réessayer plus tard.";
        
        request.setAttribute("errorMessage", safeMessage);
        forwardToError(request, response);
    }
    
    /**
     * Récupère l'ID de l'utilisateur authentifié depuis la session.
     * Retourne null si non authentifié.
     * 
     * @param request La requête HTTP
     * @return L'ID de l'utilisateur ou null
     */
    protected Long getCurrentUserId(HttpServletRequest request) {
        return SecurityUtil.getCurrentUserId(request);
    }
    
    /**
     * Vérifie que l'utilisateur est authentifié.
     * Envoie une réponse d'erreur JSON si non authentifié.
     * 
     * @param request La requête HTTP
     * @param response La réponse HTTP
     * @return L'ID de l'utilisateur si authentifié, null sinon
     * @throws IOException si une erreur d'E/S se produit
     */
    protected Long requireAuthenticatedUser(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            JsonResponseUtil.sendError(response, "Non authentifié", 
                                     HttpServletResponse.SC_UNAUTHORIZED);
        }
        return userId;
    }
    
    /**
     * Parse et valide un paramètre fileId depuis la requête.
     * 
     * @param request La requête HTTP
     * @param response La réponse HTTP (pour envoyer erreur si invalide)
     * @return L'ID du fichier si valide, null sinon
     * @throws IOException si une erreur d'E/S se produit
     */
    protected Long parseFileId(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        String fileIdParam = request.getParameter("id");
        if (fileIdParam == null || fileIdParam.trim().isEmpty()) {
            JsonResponseUtil.sendError(response, "Paramètre id requis", 
                                     HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }
        
        try {
            return Long.parseLong(fileIdParam);
        } catch (NumberFormatException e) {
            logger.warn("ID de fichier invalide: {}", fileIdParam);
            JsonResponseUtil.sendError(response, "ID de fichier invalide", 
                                     HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }
    }
    
    /**
     * Vérifie le token CSRF pour les requêtes POST.
     * Envoie une erreur appropriée selon le type de requête (API JSON ou formulaire JSP).
     * 
     * @param request La requête HTTP
     * @param response La réponse HTTP
     * @return true si le token est valide, false sinon
     * @throws IOException si une erreur d'E/S se produit
     */
    protected boolean validateCsrfToken(HttpServletRequest request, HttpServletResponse response) 
            throws IOException, ServletException {
        // Les requêtes GET ne nécessitent pas de validation CSRF
        // POST et DELETE nécessitent une validation CSRF
        String method = request.getMethod();
        if (!"POST".equalsIgnoreCase(method) && !"DELETE".equalsIgnoreCase(method)) {
            return true;
        }
        
        if (!CsrfTokenUtil.validateToken(request)) {
            logger.warn("Tentative d'accès avec token CSRF invalide depuis: {}", 
                      request.getRemoteAddr());
            
            // Vérifier si c'est une requête API (chemin commence par /api)
            String requestPath = request.getServletPath();
            if (requestPath != null && requestPath.startsWith("/api")) {
                // API : répondre en JSON
                JsonResponseUtil.sendError(response, "Token CSRF invalide ou manquant", 
                                         HttpServletResponse.SC_FORBIDDEN);
            } else {
                // Formulaire JSP : rediriger avec message flash
                HttpSession session = request.getSession(true);
                FlashMessageUtil.addError(session, "Erreur de sécurité. Veuillez réessayer.");
                
                // Rediriger vers la page actuelle (ou login pour login/register)
                String redirectPath = requestPath;
                if (redirectPath == null || redirectPath.isEmpty() || redirectPath.equals("/")) {
                    redirectPath = RouteConstants.LOGIN;
                }
                redirectWithContext(request, response, redirectPath);
            }
            return false;
        }
        
        return true;
    }
}

