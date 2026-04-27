package com.ashconversion.util;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Utilitaire Spring Boot pour le hachage et la vérification des mots de passe avec BCrypt.
 * Peut être injecté dans les services via @Autowired ou constructeur.
 */
@Component
public class PasswordUtil {

    private static final Logger logger = LoggerFactory.getLogger(PasswordUtil.class);

    // Nombre de "rounds" pour BCrypt (sécurité vs performance)
    private static final int BCRYPT_ROUNDS = 12;

    // ------------------- HACHAGE -------------------
    /**
     * Hache un mot de passe pour le stocker en base.
     *
     * @param password mot de passe en clair
     * @return hash BCrypt
     * @throws IllegalArgumentException si le mot de passe est null ou vide
     */
    public String hashPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Le mot de passe ne peut pas être vide");
        }

        try {
            String salt = BCrypt.gensalt(BCRYPT_ROUNDS); // Génération du salt
            String hash = BCrypt.hashpw(password, salt); // Calcul du hash
            logger.debug("Mot de passe haché avec succès");
            return hash;
        } catch (Exception e) {
            logger.error("Erreur lors du hachage du mot de passe", e);
            throw new RuntimeException("Erreur lors du hachage du mot de passe", e);
        }
    }

    // ------------------- VERIFICATION -------------------
    /**
     * Vérifie si un mot de passe correspond à un hash stocké.
     *
     * @param password mot de passe en clair
     * @param hash hash BCrypt stocké
     * @return true si correspond, false sinon
     */
    public boolean checkPassword(String password, String hash) {
        if (password == null || hash == null) return false;

        try {
            return BCrypt.checkpw(password, hash);
        } catch (Exception e) {
            logger.error("Erreur lors de la vérification du mot de passe", e);
            return false;
        }
    }
}
