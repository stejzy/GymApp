package org.zzpj.gymapp.scheduleservice.exeption;

public class ScheduleConflictException extends RuntimeException {
    public ScheduleConflictException(String message) {
        super(message);
    }
}
