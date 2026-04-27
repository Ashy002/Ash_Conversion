package com.ashconversion.exception;

/**
 * Exception levée lors d’un problème de stockage de fichier.
 */
public class FileUploadException extends RuntimeException {

    // Constructeur simple avec message
    public FileUploadException(String message) {
        super(message);
    }

    // Constructeur avec cause (Exception originale)
    public FileUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}


