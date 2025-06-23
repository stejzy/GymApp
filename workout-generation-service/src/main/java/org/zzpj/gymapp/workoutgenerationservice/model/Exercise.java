package org.zzpj.gymapp.workoutgenerationservice.model;

import java.util.List;

public class Exercise {
    private Long id;
    private String name;
    private String description;
    private int repetitions;
    private int sets;
    private int weight;
    private List<Muscles> muscles;

    public Exercise() {}

    public Exercise(Long id, String name, String description, int repetitions, int sets) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.repetitions = repetitions;
        this.sets = sets;
    }

    public Exercise(Long id, String name, String description, int repetitions, int sets, List<Muscles> muscles) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.repetitions = repetitions;
        this.sets = sets;
        this.muscles = muscles;
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

    public int getRepetitions() {
        return repetitions;
    }

    public void setRepetitions(int repetitions) {
        this.repetitions = repetitions;
    }

    public int getSets() {
        return sets;
    }

    public void setSets(int sets) {
        this.sets = sets;
    }

    public List<Muscles> getMuscles() {
        return muscles;
    }

    public void setMuscles(List<Muscles> muscles) {
        this.muscles = muscles;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }
}