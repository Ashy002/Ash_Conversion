package com.Ash_Conversion.controller;

import com.Ash_Conversion.constants.RouteConstants;
import com.Ash_Conversion.constants.ViewConstants;
import com.Ash_Conversion.exception.AuthenticationException;
import com.Ash_Conversion.model.dto.LoginDTO;
import com.Ash_Conversion.model.entity.User;
import com.Ash_Conversion.service.UserService;
import com.Ash_Conversion.util.FlashMessageUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Servlet pour la connexion des utilisateurs.
 */
@WebServlet(name = "LoginServlet", urlPatterns = {RouteConstants.LOGIN})
public class LoginServlet extends BaseController {
    
    private static final String SESSION_USER_ID_ATTRIBUTE = "userId";
    private final UserService userService;
    
    public LoginServlet() {
        this.userService = new UserService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Si déjà connecté, rediriger vers le dashboard
        Long userId = getCurrentUserId(request);
        if (userId != null) {
            redirectWithContext(request, response, RouteConstants.DASHBOARD);
            return;
        }
        
        // Afficher la page de connexion
        forwardToView(request, response, ViewConstants.LOGIN_VIEW);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Vérifier le token CSRF
        if (!validateCsrfToken(request, response)) {
            return; // Erreur déjà envoyée
        }
        
        try {
            // Récupérer les paramètres
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            
            // Créer le DTO
            LoginDTO loginDTO = new LoginDTO();
            loginDTO.setUsername(username);
            loginDTO.setPassword(password);
            
            // Authentifier l'utilisateur
            User user = userService.login(loginDTO);
            
            // Créer/invalider l'ancienne session pour éviter la fixation de session
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }
            
            // Créer une nouvelle session
            HttpSession session = request.getSession(true);
            session.setAttribute(SESSION_USER_ID_ATTRIBUTE, user.getId());
            
            // Configurer le timeout de session (30 minutes)
            session.setMaxInactiveInterval(30 * 60);
            
            // Message de succès
            FlashMessageUtil.addSuccess(session, 
                "Connexion réussie ! Bienvenue, " + user.getUsername() + ".");
            
            // Rediriger vers le dashboard
            redirectWithContext(request, response, RouteConstants.DASHBOARD);
            
        } catch (AuthenticationException e) {
            // Erreur d'authentification
            logger.warn("Échec de connexion: {}", e.getMessage());
            FlashMessageUtil.addError(request.getSession(), e.getMessage());
            redirectWithContext(request, response, RouteConstants.LOGIN);
            
        } catch (Exception e) {
            // Erreur inattendue
            logger.error("Erreur inattendue lors de la connexion", e);
            FlashMessageUtil.addError(request.getSession(), 
                "Une erreur est survenue lors de la connexion. Veuillez réessayer.");
            redirectWithContext(request, response, RouteConstants.LOGIN);
        }
    }
}

