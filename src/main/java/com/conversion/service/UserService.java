package com.Ash_Conversion.service;

import com.Ash_Conversion.dao.UserDAO;
import com.Ash_Conversion.exception.AuthenticationException;
import com.Ash_Conversion.model.dto.LoginDTO;
import com.Ash_Conversion.model.dto.RegisterDTO;
import com.Ash_Conversion.model.entity.User;
import com.Ash_Conversion.util.PasswordUtil;
import com.Ash_Conversion.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

/**
 * Service pour la gestion des utilisateurs (authentification, inscription).
 */
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserDAO userDAO;
    
    public UserService() {
        this.userDAO = new UserDAO();
    }
    
    /**
     * Inscrit un nouvel utilisateur.
     * 
     * @param registerDTO Les données d'inscription
     * @return L'utilisateur créé
     * @throws AuthenticationException si la validation échoue ou si l'utilisateur existe déjà
     */
    public User register(RegisterDTO registerDTO) throws AuthenticationException {
        // Validation des champs
        String username = ValidationUtil.sanitize(registerDTO.getUsername());
        String email = ValidationUtil.sanitize(registerDTO.getEmail());
        String password = registerDTO.getPassword();
        String confirmPassword = registerDTO.getConfirmPassword();
        
        // Validation username
        if (!ValidationUtil.isValidUsername(username)) {
            throw new AuthenticationException("Le nom d'utilisateur doit contenir entre 3 et 20 caractères (lettres, chiffres, underscore, tiret)");
        }
        
        // Validation email
        if (!ValidationUtil.isValidEmail(email)) {
            throw new AuthenticationException("L'email n'est pas valide");
        }
        
        // Validation mot de passe
        if (!ValidationUtil.isValidPassword(password)) {
            throw new AuthenticationException("Le mot de passe doit contenir au moins 8 caractères avec au moins une lettre et un chiffre");
        }
        
        // Vérification correspondance des mots de passe
        if (!ValidationUtil.passwordsMatch(password, confirmPassword)) {
            throw new AuthenticationException("Les mots de passe ne correspondent pas");
        }
        
        // Vérifier si l'utilisateur existe déjà
        try {
            User existingUser = userDAO.findByUsername(username);
            if (existingUser != null) {
                throw new AuthenticationException("Ce nom d'utilisateur est déjà utilisé");
            }
        } catch (Exception e) {
            logger.warn("Erreur lors de la vérification du username: {}", e.getMessage());
            // Continuer, la contrainte unique de la base gérera le doublon
        }
        
        // Vérifier si l'email existe déjà
        try {
            User existingEmail = userDAO.findByEmail(email);
            if (existingEmail != null) {
                throw new AuthenticationException("Cet email est déjà utilisé");
            }
        } catch (Exception e) {
            logger.warn("Erreur lors de la vérification de l'email: {}", e.getMessage());
            // Continuer, la contrainte unique de la base gérera le doublon
        }
        
        // Créer l'utilisateur
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(PasswordUtil.hashPassword(password));
        user.setCreatedAt(LocalDateTime.now());
        user.setIsActive(true);
        
        // Sauvegarder
        try {
            logger.debug("Appel de userDAO.create() pour username: {}", username);
            User createdUser = userDAO.create(user);
            if (createdUser == null || createdUser.getId() == null) {
                logger.error("userDAO.create() a retourné null ou utilisateur sans ID");
                throw new RuntimeException("Erreur lors de la création de l'utilisateur: l'ID n'a pas été généré");
            }
            logger.info("Nouvel utilisateur inscrit avec succès: {} (ID: {})", username, createdUser.getId());
            return createdUser;
        } catch (RuntimeException e) {
            logger.error("RuntimeException lors de la création de l'utilisateur: {}", e.getMessage(), e);
            // Capturer les erreurs de contrainte unique
            String errorMsg = e.getMessage();
            if (errorMsg != null && (errorMsg.contains("existe déjà") || 
                                     errorMsg.contains("unique") ||
                                     errorMsg.contains("duplicate"))) {
                throw new AuthenticationException("Ce nom d'utilisateur ou cet email est déjà utilisé");
            }
            // Relancer les autres erreurs avec contexte
            throw new RuntimeException("Erreur lors de la création de l'utilisateur: " + errorMsg, e);
        }
    }
    
    /**
     * Authentifie un utilisateur.
     * 
     * @param loginDTO Les données de connexion
     * @return L'utilisateur authentifié
     * @throws AuthenticationException si l'authentification échoue
     */
    public User login(LoginDTO loginDTO) throws AuthenticationException {
        String username = ValidationUtil.sanitize(loginDTO.getUsername());
        String password = loginDTO.getPassword();
        
        // Validation basique
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            throw new AuthenticationException("Le nom d'utilisateur et le mot de passe sont requis");
        }
        
        // Trouver l'utilisateur
        User user = userDAO.findByUsername(username);
        if (user == null) {
            logger.warn("Tentative de connexion avec un username inexistant: {}", username);
            throw new AuthenticationException("Nom d'utilisateur ou mot de passe incorrect");
        }
        
        // Vérifier si le compte est actif
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new AuthenticationException("Ce compte a été désactivé");
        }
        
        // Vérifier le mot de passe
        if (!PasswordUtil.checkPassword(password, user.getPasswordHash())) {
            logger.warn("Tentative de connexion avec un mot de passe incorrect pour: {}", username);
            throw new AuthenticationException("Nom d'utilisateur ou mot de passe incorrect");
        }
        
        // Mettre à jour la date de dernière connexion
        userDAO.updateLastLogin(user);
        logger.info("Utilisateur connecté avec succès: {}", username);
        
        return user;
    }
    
    /**
     * Vérifie si un utilisateur existe avec le username donné.
     * 
     * @param username Le nom d'utilisateur
     * @return true si l'utilisateur existe
     */
    public boolean userExists(String username) {
        return userDAO.findByUsername(username) != null;
    }
    
    /**
     * Trouve un utilisateur par son ID.
     * 
     * @param id L'ID de l'utilisateur
     * @return L'utilisateur ou null
     */
    public User findById(Long id) {
        return userDAO.findById(id);
    }
}


