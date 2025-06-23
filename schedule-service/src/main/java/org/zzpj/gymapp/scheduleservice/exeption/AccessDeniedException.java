package org.zzpj.gymapp.scheduleservice.exeption;

public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String message) {
        super(message);
    }
}
