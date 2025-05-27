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
import org.zzpj.gymapp.workoutgenerationservice.service.GenerationService;
import org.springframework.util.StringUtils;

@RestController
@RequestMapping("/api/workouts")
public class WorkoutController {
    private final WorkoutService workoutService;
    private final GenerationService generationService;

    @Autowired
    public WorkoutController(WorkoutService workoutService, GenerationService generationService) {
        this.workoutService = workoutService;
        this.generationService = generationService;
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

    @GetMapping("/generateTest")
    public ResponseEntity<String> generateTest() {
        String result = generationService.generateText("test");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/exercises/wger")
    public Mono<ResponseEntity<List<Exercise>>> fetchExercisesFromWger() {
        System.out.println("fetchExercisesFromWger");
        return workoutService.fetchExercisesFromWger()
                .map(exercises -> ResponseEntity.ok(exercises))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList()));
    }

    @GetMapping("/exercise/{id}")
    public Mono<ResponseEntity<String>> getExerciseById(@PathVariable Long id) {
        return workoutService.wgerTest(id)
                .map(exercise -> ResponseEntity.ok(exercise))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @GetMapping("/wgerApiTest")
    public Mono<ResponseEntity<String>> wgerApiTest() {
        System.out.println("Testing wger API connection...");
        return workoutService.wgerTest(9L) // Test z ID 1
                .map(response -> ResponseEntity.ok("WGER API Connection SUCCESS: " + response))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("WGER API Connection FAILED"));
    }

    @GetMapping("/connectionTest")
    public Mono<ResponseEntity<String>> connectionTest() {
        System.out.println("Testing basic internet connection...");
        return workoutService.simpleConnectionTest()
                .map(response -> ResponseEntity.ok("Internet Connection SUCCESS"))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Internet Connection FAILED"));
    }

    @GetMapping("/wgerBaseTest")
    public Mono<ResponseEntity<String>> wgerBaseTest() {
        System.out.println("Testing wger base API...");
        return workoutService.testWgerApi()
                .map(response -> ResponseEntity.ok("Wger Base API SUCCESS"))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Wger Base API FAILED"));
    }

    @GetMapping("/wgerStructureTest")
    public Mono<ResponseEntity<String>> wgerStructureTest() {
        System.out.println("Testing wger API structure...");
        return workoutService.testWgerExerciseStructure()
                .map(response -> ResponseEntity.ok("Wger Structure Test SUCCESS"))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Wger Structure Test FAILED"));
    }

    @PostMapping("/generate")
    public Mono<ResponseEntity<String>> generateWorkout(
            @RequestParam(required = false, defaultValue = "intermediate") String level,
            @RequestParam(required = false, defaultValue = "upper_body") String targetArea,
            @RequestParam(required = false, defaultValue = "60") int durationMinutes) {
        
        System.out.println("Generating workout for level: " + level + ", target: " + targetArea + ", duration: " + durationMinutes + " minutes");
        
        return workoutService.fetchExercisesFromWger()
                .map(exercises -> {
                    if (exercises.isEmpty()) {
                        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                                .body("Unable to fetch exercises from external API");
                    }
                    
                    // Przygotowanie promptu dla LLM z listą dostępnych ćwiczeń
                    String exercisesList = formatExercisesForLLM(exercises);
                    String prompt = buildWorkoutPrompt(level, targetArea, durationMinutes, exercisesList);
                    
                    // Generowanie treningu przez LLM
                    String generatedWorkout = generationService.generateText(prompt);
                    
                    if (!StringUtils.hasText(generatedWorkout)) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Failed to generate workout");
                    }
                    
                    return ResponseEntity.ok(generatedWorkout);
                })
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error occurred while generating workout"));
    }

    private String formatExercisesForLLM(List<Exercise> exercises) {
        StringBuilder sb = new StringBuilder();
        sb.append("Available exercises:\n");
        
        for (Exercise exercise : exercises) {
            sb.append("- ").append(exercise.getName())
              .append(" (ID: ").append(exercise.getId()).append(")")
              .append(" - ").append(exercise.getDescription())
              .append("\n");
        }
        
        return sb.toString();
    }

    private String buildWorkoutPrompt(String level, String targetArea, int durationMinutes, String exercisesList) {
        return String.format(
            "Create a detailed workout plan with the following requirements:\n" +
            "- Fitness level: %s\n" +
            "- Target area: %s\n" +
            "- Duration: %d minutes\n" +
            "- Use only exercises from the list below\n\n" +
            "%s\n" +
            "Please provide:\n" +
            "1. A structured workout plan with exercise selection\n" +
            "2. Number of sets and repetitions for each exercise\n" +
            "3. Rest periods between exercises\n" +
            "4. Warm-up and cool-down suggestions\n" +
            "5. Total estimated time for the workout\n\n" +
            "Format the response as a clear, easy-to-follow workout routine.",
            level, targetArea, durationMinutes, exercisesList
        );
    }
} 