package org.zzpj.gymapp.workoutgenerationservice.service;

import org.springframework.stereotype.Service;
import org.zzpj.gymapp.workoutgenerationservice.model.Workout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.zzpj.gymapp.workoutgenerationservice.model.Exercise;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class WorkoutService {
    private final Map<Long, Workout> workoutMap = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong();
    private final WebClient webClient;

    @Autowired
    public WorkoutService(@Qualifier("externalWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public List<Workout> getAllWorkouts() {
        return new ArrayList<>(workoutMap.values());
    }

    public Workout getWorkoutById(Long id) {
        return workoutMap.get(id);
    }

    public Workout createWorkout(Workout workout) {
        long id = idGenerator.incrementAndGet();
        workout.setId(id);
        workoutMap.put(id, workout);
        return workout;
    }

    public void deleteWorkout(Long id) {
        workoutMap.remove(id);
    }

    public Mono<List<Exercise>> fetchExercisesFromWger() {
        System.out.println("Fetching exercises from wger API...");
        return webClient.get()
                .uri("https://wger.de/api/v2/exercise/?language=2&limit=20") // language=2 for English, limit for demo
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(response -> {
                    System.out.println("Raw response from wger: " + response.substring(0, Math.min(500, response.length())));
                })
                .doOnError(error -> {
                    System.err.println("Error fetching exercises: " + error.getMessage());
                })
                .map(response -> {
                    // Simple parsing, ideally use a DTO or JSON library
                    List<Exercise> exercises = new ArrayList<>();
                    try {
                        System.out.println("Parsing JSON response...");
                        org.json.JSONObject json = new org.json.JSONObject(response);
                        System.out.println("JSON keys: " + json.keySet());
                        
                        if (json.has("results")) {
                            org.json.JSONArray results = json.getJSONArray("results");
                            System.out.println("Found " + results.length() + " results");
                            
                            // Log first object to see available fields
                            if (results.length() > 0) {
                                org.json.JSONObject firstObj = results.getJSONObject(0);
                                System.out.println("First object keys: " + firstObj.keySet());
                                System.out.println("First object full: " + firstObj.toString());
                            }
                            
                            for (int i = 0; i < results.length(); i++) {
                                org.json.JSONObject obj = results.getJSONObject(i);
                                try {
                                    Long id = obj.getLong("id");
                                    String name = obj.optString("name", "Exercise " + id);
                                    String description = obj.optString("description", "No description available");
                                    
                                    // Try other possible field names
                                    if (obj.has("license_author")) {
                                        name = "Exercise by " + obj.getString("license_author");
                                    }
                                    
                                    exercises.add(new Exercise(id, name, description, 0, 0));
                                    System.out.println("Added exercise: " + name);
                                } catch (Exception e) {
                                    System.err.println("Error processing exercise " + i + ": " + e.getMessage());
                                }
                            }
                        } else {
                            System.out.println("No 'results' field found in response");
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing JSON: " + e.getMessage());
                        e.printStackTrace();
                    }
                    System.out.println("Returning " + exercises.size() + " exercises");
                    return exercises;
                });
    }

//    public Mono<Exercise> wgerTest(Long exerciseId) {
//        return webClient.get()
//                .uri("https://wger.de/api/v2/exercise/" + exerciseId) // language=2 for English
//                .retrieve()
//                .bodyToMono(String.class)
//                .map(response -> {
//                    try {
//                        org.json.JSONObject json = new org.json.JSONObject(response);
//                        Long id = json.getLong("id");
//                        String name = json.getString("name");
//                        String description = json.getString("description");
//                        return new Exercise(id, name, description, 0, 0);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        return new Exercise(exerciseId, "Unknown", "Error fetching exercise", 0, 0);
//                    }
//                });
//    }
    public Mono<String> wgerTest(Long exerciseId) {
        System.out.println("Making request to: https://wger.de/api/v2/exercise/" + exerciseId + "/");
        return webClient.get()
                .uri("https://wger.de/api/v2/exercise/" + exerciseId + "/")
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(response -> System.out.println("Success: " + response.substring(0, Math.min(100, response.length()))))
                .doOnError(error -> System.err.println("Error occurred: " + error.getMessage()));
    }

    public Mono<String> simpleConnectionTest() {
        System.out.println("Testing simple HTTP connection...");
        return webClient.get()
                .uri("https://httpbin.org/get")
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(response -> System.out.println("Simple connection SUCCESS"))
                .doOnError(error -> System.err.println("Simple connection FAILED: " + error.getMessage()));
    }

    public Mono<String> testWgerApi() {
        System.out.println("Testing wger base API...");
        return webClient.get()
                .uri("https://wger.de/api/v2/")
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(response -> System.out.println("Wger base API SUCCESS"))
                .doOnError(error -> System.err.println("Wger base API FAILED: " + error.getMessage()));
    }

    public Mono<String> testWgerExerciseStructure() {
        System.out.println("Testing different wger endpoints...");
        return webClient.get()
                .uri("https://wger.de/api/v2/exerciseinfo/?language=2&limit=5")
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(response -> System.out.println("ExerciseInfo response: " + response.substring(0, Math.min(300, response.length()))))
                .doOnError(error -> {
                    System.err.println("ExerciseInfo failed, trying exercise translation...");
                    // Try exercise translation endpoint
                    webClient.get()
                            .uri("https://wger.de/api/v2/exercisetranslation/?language=2&limit=5")
                            .retrieve()
                            .bodyToMono(String.class)
                            .doOnSuccess(resp -> System.out.println("ExerciseTranslation response: " + resp.substring(0, Math.min(300, resp.length()))))
                            .doOnError(err -> System.err.println("ExerciseTranslation also failed: " + err.getMessage()))
                            .subscribe();
                });
    }
} 