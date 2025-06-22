package org.zzpj.gymapp.workoutgenerationservice.service;

import org.springframework.stereotype.Service;
import org.zzpj.gymapp.workoutgenerationservice.model.Workout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.zzpj.gymapp.workoutgenerationservice.model.Exercise;
import org.zzpj.gymapp.workoutgenerationservice.model.Muscles;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

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
        return fetchExercisesFromWger(null);
    }

    public Mono<List<Exercise>> fetchExercisesFromWger(List<Muscles> muscleGroups) {
        System.out.println("Fetching exercises from wger API...");
        if (muscleGroups != null && !muscleGroups.isEmpty()) {
            System.out.println("Filtering by muscle groups: " + muscleGroups.stream()
                    .map(Muscles::getFriendlyName)
                    .collect(Collectors.joining(", ")));
        }

        return fetchExercisesRawResponse(muscleGroups)
                .map(response -> {
                    List<Exercise> exercises = new ArrayList<>();
                    try {
                        System.out.println("Parsing JSON response...");
                        org.json.JSONObject json = new org.json.JSONObject(response);
                        System.out.println("JSON keys: " + json.keySet());

                        if (json.has("results")) {
                            org.json.JSONArray results = json.getJSONArray("results");
                            System.out.println("Found " + results.length() + " results");

                            // Log first object to see available fields
                            if (!results.isEmpty()) {
                                org.json.JSONObject firstObj = results.getJSONObject(0);
                                System.out.println("First object keys: " + firstObj.keySet());
                                System.out.println("First object full: " + firstObj.toString());
                            }

                            for (int i = 0; i < results.length(); i++) {
                                org.json.JSONObject obj = results.getJSONObject(i);
                                Exercise exercise = parseJsonToExercise(obj);
                                if (exercise != null) {
                                    exercises.add(exercise);
                                    System.out.println("Added exercise: " + exercise.getName());
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

    private Mono<String> fetchExercisesRawResponse() {
        return fetchExercisesRawResponse(null);
    }

    private Mono<String> fetchExercisesRawResponse(List<Muscles> muscleGroups) {
        StringBuilder uriBuilder = new StringBuilder("https://wger.de/api/v2/exerciseinfo/?language=2&limit=20");

        if (muscleGroups != null && !muscleGroups.isEmpty()) {
            for (Muscles muscle : muscleGroups) {
                uriBuilder.append("&muscles=").append(muscle.getId());
            }
        }

        String uri = uriBuilder.toString();
        System.out.println("Making request to: " + uri);

        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(response -> {
                    System.out.println("Raw response from wger exerciseinfo: " + response.substring(0, Math.min(500, response.length())));
                })
                .doOnError(error -> {
                    System.err.println("Error fetching exercises: " + error.getMessage());
                });
    }

    private Exercise parseJsonToExercise(org.json.JSONObject obj) {
        try {
            Long id = obj.getLong("id");
            String name = "Exercise " + id; // default fallback
            String description = "No description available"; // default fallback
            List<Muscles> muscles = new ArrayList<>();

            // Parse muscles
            if (obj.has("muscles")) {
                org.json.JSONArray musclesArray = obj.getJSONArray("muscles");
                for (int i = 0; i < musclesArray.length(); i++) {
                    org.json.JSONObject mObj = musclesArray.getJSONObject(i);
                    int muscleId = mObj.getInt("id");
                    Muscles muscle = findMuscleById(muscleId);
                    if (muscle != null) {
                        muscles.add(muscle);
                    }
                }
            }

            if (obj.has("muscles_secondary")) {
                org.json.JSONArray secArray = obj.getJSONArray("muscles_secondary");
                for (int i = 0; i < secArray.length(); i++) {
                    org.json.JSONObject mObj = secArray.getJSONObject(i);
                    int muscleId = mObj.getInt("id");
                    Muscles muscle = findMuscleById(muscleId);
                    if (muscle != null && !muscles.contains(muscle)) {
                        muscles.add(muscle);
                    }
                }
            }

            // Look for translations array
            if (obj.has("translations")) {
                org.json.JSONArray translations = obj.getJSONArray("translations");

                // Find English translation (language: 2)
                for (int i = 0; i < translations.length(); i++) {
                    org.json.JSONObject translation = translations.getJSONObject(i);

                    // Check if this is English translation
                    if (translation.has("language") && translation.getInt("language") == 2) {
                        name = translation.optString("name", name);
                        description = translation.optString("description", description);
                        break; // Found English translation, stop looking
                    }
                }

                // If no English translation found, use first available translation
                if (name.equals("Exercise " + id) && translations.length() > 0) {
                    org.json.JSONObject firstTranslation = translations.getJSONObject(0);
                    name = firstTranslation.optString("name", name);
                    description = firstTranslation.optString("description", description);
                    System.out.println("Used non-English translation for exercise " + id);
                }
            }

            System.out.println("Parsed exercise - ID: " + id + ", Name: " + name + ", Muscles: " +
                    muscles.stream().map(Muscles::getFriendlyName).collect(Collectors.joining(", ")));
            return new Exercise(id, name, description, 0, 0, muscles);
        } catch (Exception e) {
            System.err.println("Error processing exercise: " + e.getMessage());
            return null;
        }
    }

    private Muscles findMuscleById(int muscleId) {
        for (Muscles muscle : Muscles.values()) {
            if (muscle.getId() == muscleId) {
                return muscle;
            }
        }
        return null;
    }

    public List<Muscles> getAllAvailableMuscles() {
        return Arrays.asList(Muscles.values());
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