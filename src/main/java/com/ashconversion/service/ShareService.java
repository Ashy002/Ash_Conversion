package com.ashconversion.service;

import com.ashconversion.exception.ShareException;
import com.ashconversion.modele.entity.FileJob;
import com.ashconversion.modele.entity.ShareToken;
import com.ashconversion.modele.enums.ConversionStatus;
import com.ashconversion.repository.ShareTokenRepository;
import com.ashconversion.util.TokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service Spring Boot pour la gestion des tokens de partage.
 */
@Service
public class ShareService {

    private static final Logger logger = LoggerFactory.getLogger(ShareService.class);

    private final ShareTokenRepository shareTokenRepository;

    /**
     * Injection 
     */
    @Autowired
    public ShareService(ShareTokenRepository shareTokenRepository) {
        this.shareTokenRepository = shareTokenRepository;
    }

    /**
     * Génère un token de partage pour un fichier déjà converti.
     */
   @Transactional
    public ShareToken generateShareToken(FileJob fileJob) throws ShareException {
        if (fileJob == null) {
            throw new ShareException("Fichier invalide");
        }

        if (fileJob.getStatus() != ConversionStatus.COMPLETED) {
            throw new ShareException("Le fichier doit être converti avant le partage");
        }

        

        String token = TokenUtil.generateToken(64);

        ShareToken shareToken = new ShareToken();
        shareToken.setFileJob(fileJob);
        shareToken.setToken(token);
        // On définit une expiration propre
        shareToken.setExpiresAt(LocalDateTime.now().plusHours(24));
        shareToken.setMaxAccess(10);
        shareToken.setAccessCount(0);

        logger.info("Nouveau token de partage généré pour le fichier : {}", fileJob.getOriginalFilename());
        return shareTokenRepository.save(shareToken);
    }
    /**
     * Accès à un fichier via token public.
     */
    @Transactional
    public FileJob getFileByToken(String token) throws ShareException {

        if (token == null || token.isBlank()) {
            throw new ShareException("Token invalide");
        }

        ShareToken shareToken = shareTokenRepository
                .findByToken(token)
                .orElseThrow(() -> new ShareException("Token introuvable"));

        // Vérification expiration
        if (shareToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ShareException("Le token a expiré");
        }

        // Vérification limite d'accès
        if (shareToken.getAccessCount() >= shareToken.getMaxAccess()) {
            throw new ShareException("Nombre maximum d'accès atteint");
        }

        // Mise à jour du compteur
        shareToken.setAccessCount(shareToken.getAccessCount() + 1);
        shareTokenRepository.save(shareToken);

        logger.debug("Accès autorisé via token {}", token);
        return shareToken.getFileJob();
    }
}
