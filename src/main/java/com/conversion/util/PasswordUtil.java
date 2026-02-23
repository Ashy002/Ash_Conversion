package com.Ash_Conversion.util;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilitaire pour le hachage et la vérification de mots de passe avec BCrypt.
 */
public class PasswordUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(PasswordUtil.class);
    private static final int BCRYPT_ROUNDS = 12; // Coût de hachage (plus élevé = plus sécurisé mais plus lent)
    
    /**
     * Hache un mot de passe avec BCrypt.
     * 
     * @param password Le mot de passe en clair
     * @return Le hash BCrypt du mot de passe
     * @throws IllegalArgumentException si le mot de passe est null ou vide
     */
    public static String hashPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Le mot de passe ne peut pas être vide");
        }
        
        try {
            String salt = BCrypt.gensalt(BCRYPT_ROUNDS);
            String hash = BCrypt.hashpw(password, salt);
            logger.debug("Mot de passe haché avec succès");
            return hash;
        } catch (Exception e) {
            logger.error("Erreur lors du hachage du mot de passe", e);
            throw new RuntimeException("Erreur lors du hachage du mot de passe", e);
        }
    }
    
    /**
     * Vérifie si un mot de passe correspond à un hash BCrypt.
     * 
     * @param password Le mot de passe en clair à vérifier
     * @param hash Le hash BCrypt stocké
     * @return true si le mot de passe correspond, false sinon
     */
    public static boolean checkPassword(String password, String hash) {
        if (password == null || hash == null) {
            return false;
        }
        
        try {
            return BCrypt.checkpw(password, hash);
        } catch (Exception e) {
            logger.error("Erreur lors de la vérification du mot de passe", e);
            return false;
        }
    }
}

