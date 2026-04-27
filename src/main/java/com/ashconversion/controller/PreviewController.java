package com.ashconversion.controller;

import com.ashconversion.constants.RouteConstants;
import com.ashconversion.modele.entity.FileJob;
import com.ashconversion.modele.enums.ConversionStatus;
import com.ashconversion.service.FileJobService;
import com.ashconversion.util.FileStreamUtil;
import com.ashconversion.util.MimeTypeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import java.util.Map;
import java.io.File;

/**
 * Prévisualisation des fichiers convertis
 *
 * - PDF : affichage inline
 * - Autres formats : message JSON
 */
@Controller
public class PreviewController {

    private static final Logger logger = LoggerFactory.getLogger(PreviewController.class);

    private final FileJobService fileJobService;

    public PreviewController(FileJobService fileJobService) {
        this.fileJobService = fileJobService;
    }

    @GetMapping(RouteConstants.API_PREVIEW)
    public ResponseEntity<?> previewFile(
            @RequestParam("id") Long fileId,
            HttpSession session
    ) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "message", "Utilisateur non authentifié"
            ));
        }

        FileJob fileJob = fileJobService.findByIdAndOwner(fileId, userId);
        if (fileJob == null) {
            return ResponseEntity.status(404).body(Map.of(
                    "message", "Fichier introuvable"
            ));
        }

        if (fileJob.getStatus() != ConversionStatus.COMPLETED) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Fichier non converti"
            ));
        }

        File file = new File(fileJob.getOutputPath());
        if (!file.exists()) {
            return ResponseEntity.status(404).body(Map.of(
                    "message", "Fichier introuvable sur le serveur"
            ));
        }

        boolean isPdf = fileJob.getOutputFilename().toLowerCase().endsWith(".pdf");

        if (!isPdf) {
            return ResponseEntity.ok(Map.of(
                    "previewable", false,
                    "message", "Prévisualisation disponible uniquement pour les PDF"
            ));
        }

        // Déterminer le type MIME
        String mimeType = MimeTypeUtil.getMimeType(fileJob.getOutputFilename());

        boolean inline = true; // PDF => affichage dans le navigateur

        logger.info("Prévisualisation du fichier {}", fileJob.getId());

        // Retourner la réponse Spring Boot avec streaming
        return FileStreamUtil.streamFile(
                file,
                mimeType,
                fileJob.getOutputFilename(),
                inline
        );
    }
}
