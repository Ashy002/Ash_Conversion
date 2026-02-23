package com.Ash_Conversion.util;

import com.Ash_Conversion.exception.FileUploadException;
import jakarta.servlet.http.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Utilitaire pour la validation et manipulation des fichiers.
 */
public class FileUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);
    
    // Types MIME autorisés
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
        "application/pdf",                                    // PDF
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // DOCX
        "application/msword",                                 // DOC (ancien format)
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // XLSX
        "application/vnd.ms-excel"                            // XLS (ancien format)
    );
    
    // Extensions autorisées
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
        "pdf", "docx", "doc", "xlsx", "xls"
    );
    
    /**
     * Valide le type de fichier (MIME type et extension).
     * 
     * @param filePart Le Part du fichier uploadé
     * @return true si le type est valide
     * @throws FileUploadException si le type n'est pas valide
     */
    public static boolean isValidFileType(Part filePart) throws FileUploadException {
        if (filePart == null) {
            throw new FileUploadException("Aucun fichier fourni");
        }
        
        String contentType = filePart.getContentType();
        String filename = filePart.getSubmittedFileName();
        
        if (filename == null || filename.isEmpty()) {
            throw new FileUploadException("Le nom du fichier est vide");
        }
        
        // Vérifier l'extension
        String extension = getFileExtension(filename);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new FileUploadException(
                "Type de fichier non autorisé. Types acceptés: PDF, DOCX, DOC, XLSX, XLS");
        }
        
        // Vérifier le MIME type
        if (contentType != null && !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            logger.warn("MIME type non autorisé: {} pour le fichier: {}", contentType, filename);
            // On accepte quand même si l'extension est bonne (certains navigateurs envoient des MIME types incorrects)
        }
        
        return true;
    }
    
    /**
     * Valide la taille du fichier.
     * 
     * @param filePart Le Part du fichier uploadé
     * @return true si la taille est valide
     * @throws FileUploadException si la taille dépasse la limite
     */
    public static boolean isValidFileSize(Part filePart) throws FileUploadException {
        if (filePart == null) {
            throw new FileUploadException("Aucun fichier fourni");
        }
        
        long maxSize = ConfigUtil.getLongProperty("upload.max.size", 104857600L); // 100 MB par défaut
        long fileSize = filePart.getSize();
        
        if (fileSize <= 0) {
            throw new FileUploadException("Le fichier est vide");
        }
        
        if (fileSize > maxSize) {
            long maxSizeMB = maxSize / (1024 * 1024);
            throw new FileUploadException(
                String.format("Le fichier est trop volumineux. Taille maximum: %d MB", maxSizeMB));
        }
        
        return true;
    }
    
    /**
     * Extrait l'extension d'un nom de fichier.
     * 
     * @param filename Le nom du fichier
     * @return L'extension (sans le point)
     */
    public static String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }
    
    /**
     * Nettoie et sécurise un nom de fichier.
     * Supprime les caractères dangereux et limite la longueur.
     * 
     * @param filename Le nom du fichier original
     * @return Le nom de fichier nettoyé
     */
    public static String sanitizeFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "file";
        }
        
        // Remplacer les caractères dangereux
        String sanitized = filename
            .replaceAll("[^a-zA-Z0-9._-]", "_")  // Garder seulement lettres, chiffres, ., _, -
            .replaceAll("\\.\\.", "_")            // Éviter les .. (path traversal)
            .replaceAll("_{2,}", "_")             // Remplacer plusieurs _ par un seul
            .trim();
        
        // Limiter la longueur
        if (sanitized.length() > 255) {
            String extension = getFileExtension(sanitized);
            String nameWithoutExt = sanitized.substring(0, sanitized.lastIndexOf('.'));
            sanitized = nameWithoutExt.substring(0, Math.min(255 - extension.length() - 1, nameWithoutExt.length())) 
                       + "." + extension;
        }
        
        return sanitized;
    }
    
    /**
     * Génère un nom de fichier unique pour le stockage.
     * Format: timestamp_random_originalname
     * 
     * @param originalFilename Le nom de fichier original
     * @return Un nom de fichier unique
     */
    public static String generateUniqueFilename(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String nameWithoutExt = originalFilename.substring(0, 
            originalFilename.lastIndexOf('.') != -1 ? originalFilename.lastIndexOf('.') : originalFilename.length());
        
        String sanitized = sanitizeFilename(nameWithoutExt);
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 10000);
        
        return String.format("%d_%04d_%s.%s", timestamp, random, sanitized, extension);
    }
    
    /**
     * Formate la taille d'un fichier en format lisible.
     * 
     * @param bytes La taille en bytes
     * @return La taille formatée (KB, MB, GB)
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
}
