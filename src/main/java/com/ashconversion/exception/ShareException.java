package com.ashconversion.exception;

/**
 * Exception métier liée au partage de fichiers.
 */
public class ShareException extends RuntimeException {

    public ShareException(String message) {
        super(message);
    }

    public ShareException(String message, Throwable cause) {
        super(message, cause);
    }
}
