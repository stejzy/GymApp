package org.zzpj.gymapp.workoutgenerationservice.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.stream.Stream;

public enum Muscles {
    SHOULDERS(2, "Shoulders"),
    BICEPS(1, "Biceps"),
    HAMSTRINGS(11, "Hamstrings"),
    BRACHIALIS(13, "Brachialis"),
    CALVES(7, "Calves"),
    GLUTES(8, "Glutes"),
    LATS(12, "Lats"),
    OBLIQUES(14, "Obliquus externus abdominis"),
    CHEST(4, "Chest"),
    QUADS(10, "Quads"),
    ABS(6, "Abs"),
    SERRATUS_ANTERIOR(3, "Serratus anterior"),
    SOLEUS(15, "Soleus"),
    TRAPEZIUS(9, "Trapezius"),
    TRICEPS(5, "Triceps");

    private final int id;
    private final String friendlyName;

    Muscles(int id, String friendlyName) {
        this.id = id;
        this.friendlyName = friendlyName;
    }

    public int getId() {
        return id;
    }

    @JsonValue
    public String getFriendlyName() {
        return friendlyName;
    }

    @JsonCreator
    public static Muscles fromString(String value) {
        return Stream.of(Muscles.values())
                .filter(muscle -> muscle.friendlyName.equalsIgnoreCase(value) || muscle.name().equalsIgnoreCase(value))
                .findFirst()
                .orElse(null);
    }
}