package com.sentinelbank.finguard.exception;

/**
 * Custom exception thrown when a user registration fails because the username or email already exists.
 */
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
