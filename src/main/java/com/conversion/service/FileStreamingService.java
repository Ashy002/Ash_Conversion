package com.Ash_Conversion.service;

import com.Ash_Conversion.util.FilenameSanitizer;
import com.Ash_Conversion.util.MimeTypeUtil;
import com.Ash_Conversion.util.SecurityUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Service pour le streaming de fichiers vers la réponse HTTP.
 * Centralise la logique de streaming pour éviter la duplication.
 */
public class FileStreamingService {
    
    private static final Logger logger = LoggerFactory.getLogger(FileStreamingService.class);
    private static final int BUFFER_SIZE = 4096;
    
    /**
     * Stream un fichier vers la réponse HTTP.
     * 
     * @param file Le fichier à streamer
     * @param response La réponse HTTP
     * @param filename Le nom du fichier pour le Content-Disposition
     * @param inline true pour afficher dans le navigateur, false pour forcer le téléchargement
     * @throws IOException si une erreur d'E/S se produit
     */
    public void streamFile(File file, HttpServletResponse response, String filename, boolean inline) 
            throws IOException {
        
        if (file == null || !file.exists()) {
            throw new IOException("Le fichier n'existe pas: " + (file != null ? file.getPath() : "null"));
        }
        
        // Valider le chemin contre path traversal
        if (!SecurityUtil.validatePath(file.getAbsolutePath())) {
            throw new IOException("Chemin de fichier invalide: " + file.getAbsolutePath());
        }
        
        // Déterminer le contentType
        String contentType = MimeTypeUtil.getMimeType(filename);
        
        // Configurer les headers HTTP
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
    
    /**
     * Stream un fichier en mode téléchargement (attachment).
     * 
     * @param file Le fichier à streamer
     * @param response La réponse HTTP
     * @param filename Le nom du fichier
     * @throws IOException si une erreur d'E/S se produit
     */
    public void streamFileForDownload(File file, HttpServletResponse response, String filename) 
            throws IOException {
        streamFile(file, response, filename, false);
    }
    
    /**
     * Stream un fichier en mode prévisualisation (inline).
     * 
     * @param file Le fichier à streamer
     * @param response La réponse HTTP
     * @param filename Le nom du fichier
     * @throws IOException si une erreur d'E/S se produit
     */
    public void streamFileForPreview(File file, HttpServletResponse response, String filename) 
            throws IOException {
        streamFile(file, response, filename, true);
    }
}

