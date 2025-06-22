package org.zzpj.gymapp.scheduleservice.exeption;

public class TrainerBusyException extends RuntimeException {
    public TrainerBusyException(String message) {
        super(message);
    }
}
