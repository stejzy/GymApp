package org.zzpj.gymapp.scheduleservice.exeption;

public class TrainerNotAssignedToGymException extends RuntimeException {
    public TrainerNotAssignedToGymException(String message) {
        super(message);
    }
}
