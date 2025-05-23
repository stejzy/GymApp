package org.zzpj.gymapp.workoutgenerationservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.zzpj.gymapp.workoutgenerationservice.model.Workout;
import org.zzpj.gymapp.workoutgenerationservice.service.WorkoutService;
import reactor.core.publisher.Mono;
import org.zzpj.gymapp.workoutgenerationservice.model.Exercise;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/workouts")
public class WorkoutController {
    private final WorkoutService workoutService;

    @Autowired
    public WorkoutController(WorkoutService workoutService) {
        this.workoutService = workoutService;
    }

    @GetMapping
    public ResponseEntity<List<Workout>> getAllWorkouts() {
        List<Workout> workouts = workoutService.getAllWorkouts();
        System.out.println("big momma");
        return ResponseEntity.status(HttpStatus.CREATED).body(workouts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Workout> getWorkoutById(@PathVariable Long id) {
        Workout workout = workoutService.getWorkoutById(id);
        if (workout == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(workout);
    }

    @PostMapping
    public ResponseEntity<Workout> createWorkout(@RequestBody Workout workout) {
        Workout created = workoutService.createWorkout(workout);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkout(@PathVariable Long id) {
        Workout workout = workoutService.getWorkoutById(id);
        if (workout == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        workoutService.deleteWorkout(id);
        return ResponseEntity.noContent().build();
    }

//    @GetMapping("/exercises/wger")
//    public Mono<ResponseEntity<List<Exercise>>> fetchExercisesFromWger() {
//        return workoutService.fetchExercisesFromWger()
//                .map(exercises -> ResponseEntity.ok(exercises))
//                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList()));
//    }
} 