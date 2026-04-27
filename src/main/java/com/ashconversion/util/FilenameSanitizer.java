package com.ashconversion.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Nettoie les noms de fichiers pour Content-Disposition
 */
@Component
public class FilenameSanitizer {

    private static final Logger logger = LoggerFactory.getLogger(FilenameSanitizer.class);

    public static  String sanitizeForContentDisposition(String filename) {
        if (filename == null || filename.isEmpty()) return "file";

        String sanitized = filename.replace("\r", "")
                .replace("\n", "")
                .replace("\"", "'")
                .replace("\\", "_")
                .replaceAll("[\\x00-\\x1F\\x7F]", "_"); // caractères de contrôle

        if (sanitized.length() > 200) {
            int lastDot = sanitized.lastIndexOf('.');
            if (lastDot > 0) {
                String ext = sanitized.substring(lastDot);
                String name = sanitized.substring(0, lastDot);
                sanitized = name.substring(0, Math.min(200 - ext.length(), name.length())) + ext;
            } else {
                sanitized = sanitized.substring(0, 200);
            }
        }

        return sanitized;
    }
}
