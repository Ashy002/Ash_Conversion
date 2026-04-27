package com.conversion.exception;

public class ShareException extends Exception {
    
    public ShareException(String message) {
        super(message);
    }
    
    public ShareException(String message, Throwable cause) {
        super(message, cause);
    }
}

