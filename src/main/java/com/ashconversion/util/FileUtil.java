package com.ashconversion.util;

import com.ashconversion.exception.FileUploadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Component
public class FileUtil {

    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-excel"
    );

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "pdf", "docx", "doc", "xlsx", "xls"
    );

    public boolean isValidFileType(MultipartFile file) throws FileUploadException {
        if (file == null || file.isEmpty()) throw new FileUploadException("Aucun fichier fourni");

        String filename = file.getOriginalFilename();
        String contentType = file.getContentType();

        if (filename == null || filename.isEmpty()) {
            throw new FileUploadException("Le nom du fichier est vide");
        }

        String extension = getFileExtension(filename);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new FileUploadException("Type de fichier non autorisé");
        }

        if (contentType != null && !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            logger.warn("MIME type non autorisé: {} pour le fichier: {}", contentType, filename);
        }

        return true;
    }

    public boolean isValidFileSize(MultipartFile file, long maxSize) throws FileUploadException {
        if (file == null) throw new FileUploadException("Aucun fichier fourni");

        long size = file.getSize();
        if (size <= 0) throw new FileUploadException("Le fichier est vide");
        if (size > maxSize) throw new FileUploadException("Fichier trop volumineux. Max: " + maxSize + " bytes");

        return true;
    }

    public String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) return "";
        int dot = filename.lastIndexOf('.');
        return (dot == -1) ? "" : filename.substring(dot + 1).toLowerCase();
    }

    public String sanitizeFilename(String filename) {
        if (filename == null || filename.isEmpty()) return "file";
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_")
                       .replaceAll("\\.\\.", "_")
                       .replaceAll("_{2,}", "_")
                       .trim();
    }

    public String generateUniqueFilename(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String nameWithoutExt = originalFilename.substring(0,
                originalFilename.lastIndexOf('.') != -1 ? originalFilename.lastIndexOf('.') : originalFilename.length());
        String sanitized = sanitizeFilename(nameWithoutExt);
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 10000);
        return String.format("%d_%04d_%s.%s", timestamp, random, sanitized, extension);
    }
}
