package com.Ash_Conversion.util;

import java.security.SecureRandom;

/**
 * Utilitaire pour la génération de tokens sécurisés.
 */
public class TokenUtil {
    
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom random = new SecureRandom();
    
    /**
     * Génère un token aléatoire sécurisé de la longueur spécifiée.
     * 
     * @param length La longueur du token
     * @return Le token généré
     */
    public static String generateToken(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("La longueur du token doit être positive");
        }
        
        StringBuilder token = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            token.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        
        return token.toString();
    }
}
