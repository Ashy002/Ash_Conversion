package com.Ash_Conversion.service;

import com.Ash_Conversion.util.ConfigUtil;
import com.Ash_Conversion.dao.ShareTokenDAO;
import com.Ash_Conversion.exception.ShareException;
import com.Ash_Conversion.model.entity.FileJob;
import com.Ash_Conversion.model.entity.ShareToken;
import com.Ash_Conversion.util.TokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

/**
 * Service pour la gestion du partage de fichiers via tokens.
 */
public class ShareService {
    
    private static final Logger logger = LoggerFactory.getLogger(ShareService.class);
    
    private final ShareTokenDAO shareTokenDAO;
    private final FileJobService fileJobService;
    
    public ShareService() {
        this.shareTokenDAO = new ShareTokenDAO();
        this.fileJobService = new FileJobService();
    }
    
    /**
     * Génère un token de partage pour un FileJob.
     * 
     * @param fileJob Le FileJob à partager
     * @return Le ShareToken créé
     * @throws ShareException si le FileJob n'est pas converti ou si une erreur survient
     */
    public ShareToken generateShareToken(FileJob fileJob) throws ShareException {
        if (fileJob == null) {
            throw new ShareException("Le FileJob ne peut pas être null");
        }
        
        // Vérifier que le fichier est converti
        if (fileJob.getStatus() != com.Ash_Conversion.model.enums.ConversionStatus.COMPLETED) {
            throw new ShareException("Le fichier doit être converti avant d'être partagé");
        }
        
        // Générer le token
        int tokenLength = ConfigUtil.getIntProperty("share.token.length", 64);
        String token = TokenUtil.generateToken(tokenLength);
        
        // Calculer la date d'expiration
        int expiryHours = ConfigUtil.getIntProperty("share.token.expiry.hours", 24);
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(expiryHours);
        
        // Créer le ShareToken
        ShareToken shareToken = new ShareToken();
        shareToken.setFileJob(fileJob);
        shareToken.setToken(token);
        shareToken.setExpiresAt(expiresAt);
        shareToken.setMaxAccess(ConfigUtil.getIntProperty("share.token.max.access", 10));
        shareToken.setAccessCount(0);
        shareToken.setCreatedAt(LocalDateTime.now());
        
        try {
            shareTokenDAO.create(shareToken);
            logger.info("Token de partage généré pour FileJob: {}", fileJob.getId());
            return shareToken;
        } catch (Exception e) {
            logger.error("Erreur lors de la génération du token de partage", e);
            throw new ShareException("Erreur lors de la génération du token de partage", e);
        }
    }
    
    /**
     * Récupère un FileJob via son token de partage.
     * Vérifie l'expiration et le nombre d'accès.
     * 
     * @param token Le token de partage
     * @return Le FileJob associé
     * @throws ShareException si le token est invalide, expiré ou dépassé
     */
    public FileJob getFileByToken(String token) throws ShareException {
        if (token == null || token.isEmpty()) {
            throw new ShareException("Token invalide");
        }
        
        ShareToken shareToken = shareTokenDAO.findByToken(token);
        if (shareToken == null) {
            throw new ShareException("Token introuvable");
        }
        
        // Vérifier l'expiration
        if (shareToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ShareException("Le token a expiré");
        }
        
        // Vérifier le nombre d'accès
        if (shareToken.getAccessCount() >= shareToken.getMaxAccess()) {
            throw new ShareException("Le nombre maximum d'accès a été atteint");
        }
        
        // Incrémenter le compteur d'accès
        shareToken.setAccessCount(shareToken.getAccessCount() + 1);
        shareTokenDAO.update(shareToken);
        
        logger.debug("Accès au fichier via token: {}", token);
        return shareToken.getFileJob();
    }
}
