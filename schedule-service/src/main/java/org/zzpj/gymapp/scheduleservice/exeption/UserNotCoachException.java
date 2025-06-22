package org.zzpj.gymapp.scheduleservice.exeption;

public class UserNotCoachException extends RuntimeException {
    public UserNotCoachException(String message) {
        super(message);
    }
}
