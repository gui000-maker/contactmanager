package com.example.contactmanager.exception;


/**
 * Thrown when a refresh token exists but has passed its expiry date.
 * Maps to 401 Unauthorized — the user must log in again.
 */
public class TokenExpiredException extends RuntimeException {

    public TokenExpiredException(String message) {
        super(message);
    }
}
