package org.zzpj.gymapp.userservice.exception;

public class UserProfileNotFoundException extends RuntimeException {
    public UserProfileNotFoundException() {
        super("User profile not found");
    }

    public UserProfileNotFoundException(String message) {
        super(message);
    }

    public UserProfileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
} 