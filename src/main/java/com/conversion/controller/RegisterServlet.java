package com.Ash_Conversion.controller;

import com.Ash_Conversion.constants.RouteConstants;
import com.Ash_Conversion.constants.ViewConstants;
import com.Ash_Conversion.exception.AuthenticationException;
import com.Ash_Conversion.model.dto.RegisterDTO;
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
 * Servlet pour l'inscription des utilisateurs.
 */
@WebServlet(name = "RegisterServlet", urlPatterns = {RouteConstants.REGISTER})
public class RegisterServlet extends BaseController {
    
    private final UserService userService;
    
    public RegisterServlet() {
        this.userService = new UserService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Afficher la page d'inscription
        forwardToView(request, response, ViewConstants.REGISTER_VIEW);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Vérifier le token CSRF
        if (!validateCsrfToken(request, response)) {
            logger.warn("Tentative d'inscription avec CSRF invalide depuis: {}", request.getRemoteAddr());
            return; // Erreur déjà envoyée
        }
        
        try {
            // Récupérer les paramètres
            String username = request.getParameter("username");
            String email = request.getParameter("email");
            String password = request.getParameter("password");
            String confirmPassword = request.getParameter("confirmPassword");
            
            logger.info("Tentative d'inscription pour username: {}, email: {}", username, email);
            
            // Validation basique des paramètres
            if (username == null || username.trim().isEmpty() ||
                email == null || email.trim().isEmpty() ||
                password == null || password.isEmpty() ||
                confirmPassword == null || confirmPassword.isEmpty()) {
                logger.warn("Tentative d'inscription avec champs vides");
                HttpSession session = request.getSession(true);
                FlashMessageUtil.addError(session, "Tous les champs sont requis.");
                redirectWithContext(request, response, RouteConstants.REGISTER);
                return;
            }
            
            // Créer le DTO
            RegisterDTO registerDTO = new RegisterDTO();
            registerDTO.setUsername(username);
            registerDTO.setEmail(email);
            registerDTO.setPassword(password);
            registerDTO.setConfirmPassword(confirmPassword);
            
            // Inscrire l'utilisateur
            User createdUser = userService.register(registerDTO);
            logger.info("Utilisateur créé avec succès: {} (ID: {})", createdUser.getUsername(), createdUser.getId());
            
            // Message de succès
            HttpSession session = request.getSession(true);
            FlashMessageUtil.addSuccess(session, 
                "Inscription réussie ! Vous pouvez maintenant vous connecter.");
            
            // Rediriger vers la page de connexion
            redirectWithContext(request, response, RouteConstants.DASHBOARD);
            
        } catch (AuthenticationException e) {
            // Erreur d'authentification (validation échouée, utilisateur existe, etc.)
            logger.warn("Erreur lors de l'inscription: {}", e.getMessage());
            HttpSession session = request.getSession(true);
            FlashMessageUtil.addError(session, e.getMessage());
            redirectWithContext(request, response, RouteConstants.REGISTER);
            
        } catch (RuntimeException e) {
            // Erreur de base de données ou autre erreur runtime
            logger.error("Erreur lors de l'inscription", e);
            logger.error("Type d'erreur: {}", e.getClass().getName());
            logger.error("Message: {}", e.getMessage());
            if (e.getCause() != null) {
                logger.error("Cause: {} - {}", e.getCause().getClass().getName(), e.getCause().getMessage());
            }
            // Stack trace complète pour diagnostic
            logger.error("Stack trace complète:", e);
            
            // Message utilisateur selon le type d'erreur
            String userMessage = "Une erreur est survenue lors de l'inscription.";
            if (e.getMessage() != null) {
                if (e.getMessage().contains("existe déjà") || e.getMessage().contains("unique") || e.getMessage().contains("duplicate")) {
                    userMessage = "Ce nom d'utilisateur ou cet email est déjà utilisé.";
                } else if (e.getMessage().contains("non initialisé") || e.getMessage().contains("base de données")) {
                    userMessage = "Erreur de connexion à la base de données. Veuillez redémarrer l'application.";
                }
            }
            
            HttpSession session = request.getSession(true);
            FlashMessageUtil.addError(session, userMessage);
            redirectWithContext(request, response, RouteConstants.REGISTER);
            
        } catch (Exception e) {
            // Erreur inattendue
            logger.error("Erreur inattendue lors de l'inscription", e);
            logger.error("Type d'erreur: {}", e.getClass().getName());
            logger.error("Message: {}", e.getMessage());
            if (e.getCause() != null) {
                logger.error("Cause: {} - {}", e.getCause().getClass().getName(), e.getCause().getMessage());
            }
            // Stack trace complète pour diagnostic
            logger.error("Stack trace complète:", e);
            
            HttpSession session = request.getSession(true);
            FlashMessageUtil.addError(session, 
                "Une erreur inattendue est survenue. Veuillez vérifier les logs ou réessayer.");
            redirectWithContext(request, response, RouteConstants.REGISTER);
        }
    }
}

