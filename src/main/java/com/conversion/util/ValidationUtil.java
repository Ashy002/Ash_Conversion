package com.Ash_Conversion.util;

import java.util.regex.Pattern;

/**
 * Utilitaire pour la validation des données utilisateur.
 */
public class ValidationUtil {
    
    // Pattern pour email valide (RFC 5322 simplifié)
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );
    
    // Pattern pour username (lettres, chiffres, underscore, tiret, 3-20 caractères)
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{3,20}$");
    
    // Longueur minimale du mot de passe
    private static final int MIN_PASSWORD_LENGTH = 8;
    
    /**
     * Valide un email.
     * 
     * @param email L'email à valider
     * @return true si l'email est valide
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }
    
    /**
     * Valide un nom d'utilisateur.
     * 
     * @param username Le nom d'utilisateur à valider
     * @return true si le username est valide
     */
    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return USERNAME_PATTERN.matcher(username.trim()).matches();
    }
    
    /**
     * Valide un mot de passe.
     * 
     * @param password Le mot de passe à valider
     * @return true si le mot de passe est valide
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        // Au moins 8 caractères
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return false;
        }
        // Au moins une lettre et un chiffre
        boolean hasLetter = password.chars().anyMatch(Character::isLetter);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        return hasLetter && hasDigit;
    }
    
    /**
     * Valide que deux mots de passe correspondent.
     * 
     * @param password Le mot de passe
     * @param confirmPassword La confirmation du mot de passe
     * @return true si les mots de passe correspondent
     */
    public static boolean passwordsMatch(String password, String confirmPassword) {
        if (password == null || confirmPassword == null) {
            return false;
        }
        return password.equals(confirmPassword);
    }
    
    /**
     * Nettoie une chaîne de caractères (trim et échappement basique).
     * 
     * @param input La chaîne à nettoyer
     * @return La chaîne nettoyée
     */
    public static String sanitize(String input) {
        if (input == null) {
            return null;
        }
        return input.trim();
    }
    
    /**
     * Valide un ID numérique (Long).
     * 
     * @param idStr La chaîne contenant l'ID
     * @return true si l'ID est valide
     */
    public static boolean isValidId(String idStr) {
        if (idStr == null || idStr.trim().isEmpty()) {
            return false;
        }
        try {
            Long id = Long.parseLong(idStr.trim());
            return id > 0; // Les IDs doivent être positifs
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Valide un paramètre de recherche (longueur maximale et caractères autorisés).
     * 
     * @param search Le terme de recherche
     * @return true si le terme de recherche est valide
     */
    public static boolean isValidSearchTerm(String search) {
        if (search == null) {
            return true; // Recherche vide autorisée
        }
        // Limiter la longueur pour éviter les attaques
        if (search.length() > 100) {
            return false;
        }
        // Autoriser les lettres, chiffres, espaces et quelques caractères spéciaux
        return search.matches("^[a-zA-Z0-9\\s._-]*$");
    }
    
    /**
     * Valide un paramètre de pagination.
     * 
     * @param pageStr La chaîne contenant le numéro de page
     * @return true si le numéro de page est valide
     */
    public static boolean isValidPageNumber(String pageStr) {
        if (pageStr == null || pageStr.trim().isEmpty()) {
            return true; // Page vide = page 1
        }
        try {
            int page = Integer.parseInt(pageStr.trim());
            return page > 0 && page <= 10000; // Limite raisonnable
        } catch (NumberFormatException e) {
            return false;
        }
    }
}

