package com.ashconversion.exception;

/**
 * Exception métier pour les erreurs de conversion de fichiers.
 */
public class ConversionException extends Exception {

    public ConversionException(String message) {
        super(message);
    }

    public ConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
