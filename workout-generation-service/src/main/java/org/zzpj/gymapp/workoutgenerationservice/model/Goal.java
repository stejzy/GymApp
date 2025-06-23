package org.zzpj.gymapp.workoutgenerationservice.model;

public enum Goal {
    STRENGTH,
    ENDURANCE,
    WEIGHT_LOSS,
    MUSCLE_GAIN;

    public static Goal fromString(String targetArea) {
        return Goal.valueOf(targetArea.toUpperCase());
    }
}