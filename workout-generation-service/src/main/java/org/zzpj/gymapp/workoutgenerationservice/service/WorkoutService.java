package org.zzpj.gymapp.workoutgenerationservice.service;

import org.springframework.stereotype.Service;
import org.zzpj.gymapp.workoutgenerationservice.model.Workout;
import org.springframework.beans.factory.annotation.Autowired;
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
    public WorkoutService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
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

//    public Mono<List<Exercise>> fetchExercisesFromWger() {
//        return webClient.get()
//                .uri("https://wger.de/api/v2/exercise/?language=2&limit=20") // language=2 for English, limit for demo
//                .retrieve()
//                .bodyToMono(String.class)
//                .map(response -> {
//                    // Simple parsing, ideally use a DTO or JSON library
//                    List<Exercise> exercises = new ArrayList<>();
//                    try {
//                        org.json.JSONObject json = new org.json.JSONObject(response);
//                        org.json.JSONArray results = json.getJSONArray("results");
//                        for (int i = 0; i < results.length(); i++) {
//                            org.json.JSONObject obj = results.getJSONObject(i);
//                            Long id = obj.getLong("id");
//                            String name = obj.getString("name");
//                            String description = obj.getString("description");
//                            exercises.add(new Exercise(id, name, description, 0, 0));
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    return exercises;
//                });
//    }
} 