package org.zzpj.gymapp.scheduleservice.exeption;

public class GymNotFoundException extends RuntimeException {
    public GymNotFoundException(String message) {
        super(message);
    }
}
