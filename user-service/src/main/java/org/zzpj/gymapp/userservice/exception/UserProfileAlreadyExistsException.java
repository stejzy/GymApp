package org.zzpj.gymapp.userservice.exception;

public class UserProfileAlreadyExistsException extends RuntimeException {
    public UserProfileAlreadyExistsException() {
        super("User profile already exists");
    }

    public UserProfileAlreadyExistsException(String message) {
        super(message);
    }

    public UserProfileAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
} 