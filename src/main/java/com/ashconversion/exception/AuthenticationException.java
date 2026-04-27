package com.ashconversion.exception;

/**
 * Exception levée lors d’un problème d’authentification.
 */
public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message) {
        super(message);
    }
}
