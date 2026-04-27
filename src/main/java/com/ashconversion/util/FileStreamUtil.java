package com.ashconversion.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.File;

/**
 * Utilitaire Spring Boot pour streamer des fichiers (download / preview).
 */
public final class FileStreamUtil {

    private static final Logger logger = LoggerFactory.getLogger(FileStreamUtil.class);

    private FileStreamUtil() {
        // Classe utilitaire
    }

    /**
     * Construit une réponse HTTP pour streamer un fichier.
     *
     * @param file fichier à envoyer
     * @param contentType type MIME
     * @param filename nom exposé au client
     * @param inline true = affichage navigateur, false = téléchargement
     */
    public static ResponseEntity<Resource> streamFile(
            File file,
            String contentType,
            String filename,
            boolean inline
    ) {

        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("Fichier introuvable");
        }

        Resource resource = new FileSystemResource(file);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentLength(file.length());

        String dispositionType = inline ? "inline" : "attachment";
        String safeFilename = FilenameSanitizer.sanitizeForContentDisposition(filename);

        headers.set(
                HttpHeaders.CONTENT_DISPOSITION,
                dispositionType + "; filename=\"" + safeFilename + "\""
        );

        logger.debug("Streaming fichier: {} ({} bytes)", filename, file.length());

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }
}

