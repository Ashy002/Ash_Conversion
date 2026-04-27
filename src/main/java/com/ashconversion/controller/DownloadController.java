package com.ashconversion.controller;

import com.ashconversion.constants.RouteConstants;
import com.ashconversion.modele.entity.FileJob;
import com.ashconversion.modele.enums.ConversionStatus;
import com.ashconversion.service.FileJobService;
import com.ashconversion.util.FileStreamUtil;
import com.ashconversion.util.MimeTypeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;

/**
 * Téléchargement des fichiers convertis
 * Endpoint : GET /api/download?id=...
 */
@Controller
public class DownloadController {

    private static final Logger logger = LoggerFactory.getLogger(DownloadController.class);

    private final FileJobService fileJobService;

    public DownloadController(FileJobService fileJobService) {
        this.fileJobService = fileJobService;
    }

    @GetMapping(RouteConstants.API_DOWNLOAD)
    public ResponseEntity<Resource> downloadFile(
            @RequestParam("id") Long fileId,
            HttpSession session
    ) throws IOException {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build(); // Non authentifié
        }

        FileJob fileJob = fileJobService.findByIdAndOwner(fileId, userId);
        if (fileJob == null) {
            return ResponseEntity.status(404).build(); // Fichier non trouvé
        }

        if (fileJob.getStatus() != ConversionStatus.COMPLETED) {
            return ResponseEntity.status(400).build(); // Fichier non converti
        }

        File file = new File(fileJob.getOutputPath());
        if (!file.exists()) {
            logger.error("Fichier introuvable: {}", fileJob.getOutputPath());
            return ResponseEntity.status(404).build();
        }

        String mimeType = MimeTypeUtil.getMimeType(fileJob.getOutputFilename());
        boolean inline = false; // téléchargement

        logger.info("Téléchargement du fichier {} par utilisateur {}", 
                    fileJob.getOutputFilename(), userId);

        // Utilisation de FileStreamUtil version Spring Boot
        return FileStreamUtil.streamFile(
                file,
                mimeType,
                fileJob.getOutputFilename(),
                inline
        );
    }
}
