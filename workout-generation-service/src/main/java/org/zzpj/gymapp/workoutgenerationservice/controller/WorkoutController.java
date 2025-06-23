package org.zzpj.gymapp.workoutgenerationservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.zzpj.gymapp.workoutgenerationservice.model.*;
import org.zzpj.gymapp.workoutgenerationservice.service.WorkoutService;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import org.zzpj.gymapp.workoutgenerationservice.service.GenerationService;
import reactor.core.scheduler.Schedulers;

import java.util.Objects;
import java.util.stream.Collectors;

import static org.apache.http.HttpHeaders.AUTHORIZATION;

@RestController
public class WorkoutController {
    private final WorkoutService workoutService;
    private final GenerationService generationService;
    Logger logger = LoggerFactory.getLogger(WorkoutController.class);

    @Autowired
    public WorkoutController(WorkoutService workoutService, GenerationService generationService) {
        this.workoutService = workoutService;
        this.generationService = generationService;
    }

    @GetMapping
    public ResponseEntity<List<Workout>> getAllWorkouts() {
        List<Workout> workouts = workoutService.getAllWorkouts();
        logger.info("big momma");
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

    @GetMapping("/generateTest")
    public ResponseEntity<String> generateTest() {
        return ResponseEntity.ok("ok");
    }

    @GetMapping("/exercises/wger")
    public Mono<ResponseEntity<List<Exercise>>> fetchExercisesFromWger() {
        logger.info("fetchExercisesFromWger");
        return workoutService.fetchExercisesFromWger()
                .map(exercises -> ResponseEntity.ok(exercises))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList()));
    }

    @GetMapping("/exercises/wger/filter")
    public Mono<ResponseEntity<List<Exercise>>> fetchExercisesFromWgerByMuscles(
            @RequestParam List<String> muscleGroups) {
        logger.info("fetchExercisesFromWgerByMuscles with muscle groups: {}", muscleGroups);
        
        List<Muscles> muscles = muscleGroups.stream()
                .map(Muscles::fromString)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        if (muscles.isEmpty()) {
            return Mono.just(ResponseEntity.badRequest()
                    .body(Collections.emptyList()));
        }
        
        return workoutService.fetchExercisesFromWger(muscles)
                .map(exercises -> ResponseEntity.ok(exercises))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList()));
    }

    @GetMapping("/muscles")
    public ResponseEntity<List<Muscles>> getAllAvailableMuscles() {
        List<Muscles> muscles = workoutService.getAllAvailableMuscles();
        return ResponseEntity.ok(muscles);
    }

    @GetMapping("/exercise/{id}")
    public Mono<ResponseEntity<String>> getExerciseById(@PathVariable Long id) {
        return workoutService.wgerTest(id)
                .map(exercise -> ResponseEntity.ok(exercise))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @GetMapping("/wgerApiTest")
    public Mono<ResponseEntity<String>> wgerApiTest() {
        logger.info("Testing wger API connection...");
        return workoutService.wgerTest(9L) // Test z ID 1
                .map(response -> ResponseEntity.ok("WGER API Connection SUCCESS: " + response))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("WGER API Connection FAILED"));
    }

    @GetMapping("/connectionTest")
    public Mono<ResponseEntity<String>> connectionTest() {
        logger.info("Testing basic internet connection...");
        return workoutService.simpleConnectionTest()
                .map(response -> ResponseEntity.ok("Internet Connection SUCCESS"))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Internet Connection FAILED"));
    }

    @GetMapping("/wgerBaseTest")
    public Mono<ResponseEntity<String>> wgerBaseTest() {
        logger.info("Testing wger base API...");
        return workoutService.testWgerApi()
                .map(response -> ResponseEntity.ok("Wger Base API SUCCESS"))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Wger Base API FAILED"));
    }

    @GetMapping("/wgerStructureTest")
    public Mono<ResponseEntity<String>> wgerStructureTest() {
        logger.info("Testing wger API structure...");
        return workoutService.testWgerExerciseStructure()
                .map(response -> ResponseEntity.ok("Wger Structure Test SUCCESS"))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Wger Structure Test FAILED"));
    }

    @PostMapping("/generate")
    public Mono<ResponseEntity<Workout>> generateWorkout(
            @RequestParam(defaultValue = "strength") String targetArea,
            @RequestParam(defaultValue = "60") int durationMinutes,
            @RequestParam(required = false) List<String> muscleGroups,
            @RequestHeader(name = AUTHORIZATION) String authHeader,
            @RequestHeader("X-User-Id") Long userId
    ) {
        List<Muscles> muscles = muscleGroups == null
            ? List.of()
            : muscleGroups.stream()
                .map(Muscles::fromString)
                .filter(m -> m != null)
                .collect(Collectors.toList());

        return workoutService.fetchExercisesFromWger(muscles)
            .flatMap(candidates -> {
                if (candidates.isEmpty()) {
                    return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build());
                }
                Goal goalEnum = Goal.fromString(targetArea);

                return Mono.fromCallable(() ->
                        generationService.generateWorkout(candidates, authHeader, userId, goalEnum, durationMinutes)
                    )
                    .subscribeOn(Schedulers.boundedElastic())
                    .map(workout -> ResponseEntity.ok(workout))
                    .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
            });
    }
}