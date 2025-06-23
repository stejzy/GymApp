package org.zzpj.gymapp.workoutgenerationservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    Logger logger = LoggerFactory.getLogger(WorkoutService.class);

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
        logger.info("Fetching exercises from wger API...");
        if (muscleGroups != null && !muscleGroups.isEmpty()) {
            logger.info("Filtering by muscle groups: {}", muscleGroups.stream()
                    .map(Muscles::getFriendlyName)
                    .collect(Collectors.joining(", ")));
        }

        return fetchExercisesRawResponse(muscleGroups)
                .map(response -> {
                    List<Exercise> exercises = new ArrayList<>();
                    try {
                        logger.info("Parsing JSON response...");
                        org.json.JSONObject json = new org.json.JSONObject(response);
                        logger.info("JSON keys: {}", json.keySet());

                        if (json.has("results")) {
                            org.json.JSONArray results = json.getJSONArray("results");
                            logger.info("Found {} results", results.length());

                            if (!results.isEmpty()) {
                                org.json.JSONObject firstObj = results.getJSONObject(0);
                                logger.info("First object keys: {}", firstObj.keySet());
                                logger.info("First object full: {}", firstObj);
                            }

                            for (int i = 0; i < results.length(); i++) {
                                org.json.JSONObject obj = results.getJSONObject(i);
                                Exercise exercise = parseJsonToExercise(obj);
                                if (exercise != null) {
                                    exercises.add(exercise);
                                    logger.info("Added exercise: {}", exercise.getName());
                                }
                            }
                        } else {
                            logger.info("No 'results' field found in response");
                        }
                    } catch (Exception e) {
                        logger.error("Error parsing JSON: {}", e.getMessage());
                        e.printStackTrace();
                    }
                    logger.info("Returning {} exercises", exercises.size());
                    return exercises;
                });
    }


    private Mono<String> fetchExercisesRawResponse(List<Muscles> muscleGroups) {
        StringBuilder uriBuilder = new StringBuilder("https://wger.de/api/v2/exerciseinfo/?language=2&limit=100");

        if (muscleGroups != null && !muscleGroups.isEmpty()) {
            for (Muscles muscle : muscleGroups) {
                uriBuilder.append("&muscles=").append(muscle.getId());
            }
        }

        String uri = uriBuilder.toString();
        logger.info("Making request to: {}", uri);

        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(response -> logger.info("Raw response from wger exerciseinfo: {}", response.substring(0, Math.min(500, response.length()))))
                .doOnError(error -> logger.error("Error fetching exercises: {}", error.getMessage()));
    }

    private Exercise parseJsonToExercise(org.json.JSONObject obj) {
        try {
            Long id = obj.getLong("id");
            String name = "Exercise " + id;
            String description = "No description available";
            List<Muscles> muscles = new ArrayList<>();

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

            if (obj.has("translations")) {
                org.json.JSONArray translations = obj.getJSONArray("translations");

                for (int i = 0; i < translations.length(); i++) {
                    org.json.JSONObject translation = translations.getJSONObject(i);

                    if (translation.has("language") && translation.getInt("language") == 2) {
                        name = translation.optString("name", name);
                        description = translation.optString("description", description);
                        break;
                    }
                }

                if (name.equals("Exercise " + id) && translations.length() > 0) {
                    org.json.JSONObject firstTranslation = translations.getJSONObject(0);
                    name = firstTranslation.optString("name", name);
                    description = firstTranslation.optString("description", description);
                    logger.info("Used non-English translation for exercise {}", id);
                }
            }

            logger.info("Parsed exercise - ID: {}, Name: {}, Muscles: {}", id, name, muscles.stream().map(Muscles::getFriendlyName).collect(Collectors.joining(", ")));
            return new Exercise(id, name, description, 0, 0, muscles);
        } catch (Exception e) {
            logger.error("Error processing exercise: {}", e.getMessage());
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

    public Mono<String> wgerTest(Long exerciseId) {
        logger.info("Making request to: https://wger.de/api/v2/exercise/{}/", exerciseId);
        return webClient.get()
                .uri("https://wger.de/api/v2/exercise/" + exerciseId + "/")
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(response -> logger.info("Success: {}", response.substring(0, Math.min(100, response.length()))))
                .doOnError(error -> logger.error("Error occurred: {}", error.getMessage()));
    }
} 