package org.zzpj.gymapp.workoutgenerationservice.model;

import java.util.List;

public class Workout {
    private Long id;
    private String name;
    private String description;
    private List<Exercise> exercises;
    private ExperienceLevel experienceLevel;
    private Goal goal;

    public Workout() {}

    public Workout(Long id, String name, String description, List<Exercise> exercises, ExperienceLevel experienceLevel, Goal goal) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.exercises = exercises;
        this.experienceLevel = experienceLevel;
        this.goal = goal;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Exercise> getExercises() {
        return exercises;
    }

    public void setExercises(List<Exercise> exercises) {
        this.exercises = exercises;
    }

    public ExperienceLevel getExperienceLevel() {
        return experienceLevel;
    }

    public void setExperienceLevel(ExperienceLevel experienceLevel) {
        this.experienceLevel = experienceLevel;
    }

    public Goal getGoal() {
        return goal;
    }

    public void setGoal(Goal goal) {
        this.goal = goal;
    }
} 