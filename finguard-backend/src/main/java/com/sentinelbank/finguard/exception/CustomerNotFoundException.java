package com.sentinelbank.finguard.exception;

/**
 * Custom exception thrown when a customer is not found in the database.
 */
public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(String message) {
        super(message);
    }
}
