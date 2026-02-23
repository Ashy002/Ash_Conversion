package com.Ash_Conversion.util;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Utilitaire pour streamer des fichiers vers la réponse HTTP.
 */
public class FileStreamUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(FileStreamUtil.class);
    private static final int BUFFER_SIZE = 4096;
    
    private FileStreamUtil() {
        // Classe utilitaire
    }
    
    /**
     * Stream un fichier vers la réponse HTTP.
     * 
     * @param file Le fichier à streamer
     * @param response La réponse HTTP
     * @param contentType Le type MIME du fichier
     * @param filename Le nom du fichier pour Content-Disposition
     * @param inline true pour afficher inline, false pour forcer le téléchargement
     * @throws IOException si une erreur d'E/S se produit
     */
    public static void streamFile(File file, HttpServletResponse response, 
                                  String contentType, String filename, boolean inline) 
            throws IOException {
        if (file == null || !file.exists()) {
            throw new IOException("Le fichier n'existe pas: " + (file != null ? file.getPath() : "null"));
        }
        
        // Configurer les en-têtes HTTP
        response.setContentType(contentType);
        response.setContentLengthLong(file.length());
        
        String disposition = inline ? "inline" : "attachment";
        String sanitizedFilename = FilenameSanitizer.sanitizeForContentDisposition(filename);
        response.setHeader("Content-Disposition", 
            String.format("%s; filename=\"%s\"", disposition, sanitizedFilename));
        
        // Stream le fichier
        try (FileInputStream fis = new FileInputStream(file);
             OutputStream out = response.getOutputStream()) {
            
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
        }
        
        logger.debug("Fichier streamé: {} ({} bytes)", filename, file.length());
    }
}

