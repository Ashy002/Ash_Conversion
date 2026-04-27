package com.ashconversion.service;

import com.ashconversion.exception.AuthenticationException;
import com.ashconversion.modele.dto.LoginDTO;
import com.ashconversion.modele.dto.RegisterDTO;
import com.ashconversion.modele.entity.User;
import com.ashconversion.repository.UserRepository;
import com.ashconversion.util.PasswordUtil;
import com.ashconversion.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final ValidationUtil validationUtil;
    private final PasswordUtil passwordUtil;

    // Injection par constructeur
    public UserService(UserRepository userRepository, ValidationUtil validationUtil, PasswordUtil passwordUtil) {
        this.userRepository = userRepository;
        this.validationUtil = validationUtil;
        this.passwordUtil = passwordUtil;
    }

    // ------------------- ENREGISTREMENT -------------------
    public User register(RegisterDTO dto) throws AuthenticationException {

        String username = validationUtil.sanitize(dto.getUsername());
        String email = validationUtil.sanitize(dto.getEmail());
        String password = dto.getPassword();

        // Validation
        if (!validationUtil.isValidUsername(username)) {
            throw new AuthenticationException("Nom d'utilisateur invalide");
        }
        if (!validationUtil.isValidEmail(email)) {
            throw new AuthenticationException("Email invalide");
        }
        if (!validationUtil.isValidPassword(password)) {
            throw new AuthenticationException("Mot de passe invalide");
        }
        if (!validationUtil.passwordsMatch(password, dto.getConfirmPassword())) {
            throw new AuthenticationException("Les mots de passe ne correspondent pas");
        }

        // Vérification doublons
        if (userRepository.existsByUsername(username)) {
            throw new AuthenticationException("Nom d'utilisateur déjà utilisé");
        }
        if (userRepository.existsByEmail(email)) {
            throw new AuthenticationException("Email déjà utilisé");
        }

        // Création utilisateur
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordUtil.hashPassword(password)); // Hash du mot de passe
        user.setActive(true);                                        // Compte actif

        User savedUser = userRepository.save(user);
        logger.info("Utilisateur créé : {}", savedUser.getUsername());

        return savedUser;
    }

    // ------------------- LOGIN -------------------
    public User login(LoginDTO dto) throws AuthenticationException {

        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new AuthenticationException("Identifiants incorrects"));

        if (!user.isActive()) {
            throw new AuthenticationException("Compte désactivé");
        }

        if (!passwordUtil.checkPassword(dto.getPassword(), user.getPasswordHash())) {
            throw new AuthenticationException("Identifiants incorrects");
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        return user;
    }

   public User findById(Long id) {
    return userRepository.findById(id).orElse(null);
}
}
