package org.zzpj.gymapp.scheduleservice.exeption;

public class CoachAlreadyAssignedException extends RuntimeException {
    public CoachAlreadyAssignedException(String message) {
        super(message);
    }
}
