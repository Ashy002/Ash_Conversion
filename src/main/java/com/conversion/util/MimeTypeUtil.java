package com.Ash_Conversion.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Utilitaire pour déterminer le type MIME selon l'extension de fichier.
 */
public class MimeTypeUtil {
    
    private static final Map<String, String> MIME_TYPES = new HashMap<>();
    
    static {
        MIME_TYPES.put("pdf", "application/pdf");
        MIME_TYPES.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        MIME_TYPES.put("doc", "application/msword");
        MIME_TYPES.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        MIME_TYPES.put("xls", "application/vnd.ms-excel");
    }
    
    private MimeTypeUtil() {
        // Classe utilitaire
    }
    
    /**
     * Détermine le type MIME selon l'extension du fichier.
     * 
     * @param filename Le nom du fichier
     * @return Le type MIME ou "application/octet-stream" si inconnu
     */
    public static String getMimeType(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "application/octet-stream";
        }
        
        String lowerFilename = filename.toLowerCase();
        int lastDot = lowerFilename.lastIndexOf('.');
        if (lastDot == -1 || lastDot == lowerFilename.length() - 1) {
            return "application/octet-stream";
        }
        
        String extension = lowerFilename.substring(lastDot + 1);
        return MIME_TYPES.getOrDefault(extension, "application/octet-stream");
    }
}
