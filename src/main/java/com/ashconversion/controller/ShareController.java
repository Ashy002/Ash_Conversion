package com.ashconversion.controller;

import com.ashconversion.constants.RouteConstants;
import com.ashconversion.exception.ShareException;
import com.ashconversion.modele.entity.FileJob;
import com.ashconversion.modele.entity.ShareToken;
import com.ashconversion.modele.enums.ConversionStatus;
import com.ashconversion.service.FileJobService;
import com.ashconversion.service.ShareService;
import com.ashconversion.util.FileStreamUtil;
import com.ashconversion.util.MimeTypeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestion du partage de fichiers via token
 *
 * - POST /api/share        : génère un lien de partage
 * - GET  /share/{token}    : accès public au fichier converti
 */
@Controller
public class ShareController {

    private static final Logger logger = LoggerFactory.getLogger(ShareController.class);

    private final ShareService shareService;
    private final FileJobService fileJobService;

    public ShareController(ShareService shareService, FileJobService fileJobService) {
        this.shareService = shareService;
        this.fileJobService = fileJobService;
    }

    /**
     * Génère un token de partage pour un fichier
     */
    @PostMapping(RouteConstants.API_SHARE)
    @ResponseBody
    public Map<String, Object> generateShareToken(
            @RequestParam("fileId") Long fileId,
            HttpSession session,
            HttpServletRequest request
    ) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            throw new ShareException("Utilisateur non authentifié");
        }

        FileJob fileJob = fileJobService.findByIdAndOwner(fileId, userId);
        if (fileJob == null) {
            throw new ShareException("Fichier introuvable ou accès refusé");
        }

        ShareToken token = shareService.generateShareToken(fileJob);

        // Construire l'URL publique
        String shareUrl = request.getScheme() + "://" + request.getServerName()
                + (request.getServerPort() != 80 && request.getServerPort() != 443
                ? ":" + request.getServerPort() : "")
                + request.getContextPath()
                + RouteConstants.SHARE_PUBLIC + "/" + token.getToken();

                // PRÉPARE LE MAP POUR LE JAVASCRIPT
   
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> dataMap = new HashMap<>();

        dataMap.put("url", shareUrl);
        dataMap.put("token", token.getToken());
        dataMap.put("expiresAt", token.getExpiresAt());
        dataMap.put("maxAccess", token.getMaxAccess());

        
        response.put("success", true);
        response.put("data", dataMap);

        logger.info("Lien de partage généré avec succès pour le fichier {}", fileId);
        return response; 
    } 

    /**
     * Accès public à un fichier partagé
     */
    @GetMapping(RouteConstants.SHARE_PUBLIC + "/{token}")
    public ResponseEntity<?> accessSharedFile(
            @PathVariable String token
    ) {

        // Récupérer le FileJob associé au token
        FileJob fileJob = shareService.getFileByToken(token);

        // Vérifier le statut de conversion
        if (fileJob.getStatus() != ConversionStatus.COMPLETED) {
            throw new ShareException("Le fichier n'est pas encore converti");
        }

        File file = new File(fileJob.getOutputPath());
        if (!file.exists()) {
            throw new ShareException("Fichier introuvable");
        }

        // Déterminer le type MIME
        String mimeType = MimeTypeUtil.getMimeType(fileJob.getOutputFilename());

        // Déterminer si le fichier doit être affiché inline (PDF) ou téléchargé
        boolean inline = fileJob.getOutputFilename().toLowerCase().endsWith(".pdf");

        logger.info("Fichier partagé téléchargé (token={})", token);

        // Retourner la réponse Spring Boot avec streaming
        return FileStreamUtil.streamFile(
                file,
                mimeType,
                fileJob.getOutputFilename(),
                inline
        );
    }
}
