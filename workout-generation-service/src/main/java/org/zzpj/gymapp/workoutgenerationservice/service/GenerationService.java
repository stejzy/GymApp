package org.zzpj.gymapp.workoutgenerationservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.zzpj.gymapp.workoutgenerationservice.client.UserServiceClient;
import org.zzpj.gymapp.workoutgenerationservice.dto.UserProfileDTO;
import org.zzpj.gymapp.workoutgenerationservice.model.Exercise;
import org.zzpj.gymapp.workoutgenerationservice.model.ExperienceLevel;
import org.zzpj.gymapp.workoutgenerationservice.model.Goal;
import org.zzpj.gymapp.workoutgenerationservice.model.Workout;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GenerationService {
    private final OpenAIClient client;
    private final ObjectMapper mapper = new ObjectMapper();

    private UserServiceClient userServiceClient;

    Logger logger = LoggerFactory.getLogger(GenerationService.class);

    public GenerationService(@Value("${openai.api.key}") String apiKey, UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
        this.client = OpenAIOkHttpClient.builder()
            .apiKey(apiKey)
            .build();
    }

    public Workout generateWorkout(List<Exercise> candidates,
                                   String authHeader,
                                   Long userId,
                                   Goal goal,
                                   int durationMinutes) throws Exception {

        UserProfileDTO profile = userServiceClient.getUserProfile(authHeader, userId);

        if (profile.getLevel() == null ) {
            throw new Exception(String.format("User %s has no level", userId));
        }
        ExperienceLevel level = ExperienceLevel.valueOf(profile.getLevel().toUpperCase());

        String prompt = buildGeneratePrompt(candidates, level, goal, durationMinutes, profile);

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
            .model(ChatModel.GPT_4_1_NANO_2025_04_14)
            .addSystemMessage("You are a fitness assistant. Create a complete workout plan.")
            .addUserMessage(prompt)
            .temperature(0.2)
            .build();

        ChatCompletion completion = client.chat().completions().create(params);
        String content = completion.choices().getFirst().message().content().orElse("bruh");

        logger.info("OpenAI response: {}", content);

        try {
            JsonNode root = mapper.readTree(content);
            String workoutName = root.path("workoutName").asText();
            String workoutDesc = root.path("workoutDescription").asText();

            List<Exercise> finalExercises = new ArrayList<>();
            for (JsonNode exNode : root.path("exercises")) {
                int id = exNode.path("id").asInt();
                int sets = exNode.path("sets").asInt();
                int reps = exNode.path("repetitions").asInt();
                int weight;
                JsonNode weightNode = exNode.path("weight");
                if (weightNode.isInt()) {
                    weight = weightNode.asInt();
                } else {
                    weight = 0;
                    if (weightNode.isTextual() && weightNode.asText().equalsIgnoreCase("bodyweight")) {
                    }
                }

                candidates.stream()
                    .filter(e -> e.getId().intValue() == id)
                    .findFirst()
                    .ifPresent(base -> {
                        base.setSets(sets);
                        base.setRepetitions(reps);
                        base.setWeight(weight);
                        finalExercises.add(base);
                    });
            }

            return new Workout(null,
                               workoutName,
                               workoutDesc,
                               finalExercises,
                               level,
                               goal);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to parse OpenAI response: " + content, e);
        }
    }

    private String buildGeneratePrompt(List<Exercise> list,
                                       ExperienceLevel level,
                                       Goal goal,
                                       int duration,
                                       UserProfileDTO profile) {
        StringBuilder sb = new StringBuilder();
        sb.append("Here are the candidate exercises (id: name, muscles):\n");
        for (Exercise ex : list) {
            sb.append(ex.getId()).append(": ")
              .append(ex.getName())
              .append(" (" + ex.getMuscles().stream()
                                  .map(m -> m.getFriendlyName())
                                  .collect(Collectors.joining(", ")) + ")\n");
        }
        sb.append("\nCreate a workout plan for a user with the following profile: ");
        sb.append("experience level: ").append(level)
          .append(", goal: ").append(goal)
          .append(", total duration: ").append(duration).append(" minutes");
        if (profile != null) {
            if (profile.getBirthday() != null) {
                sb.append(", age: ").append(java.time.Period.between(profile.getBirthday(), java.time.LocalDate.now()).getYears());
            }
            if (profile.getWeight() != null) {
                sb.append(", weight: ").append(profile.getWeight()).append(" kg");
            }
        }
        sb.append(". Respond with a JSON object only, with keys, without comments:\n")
          .append("- workoutName (string)\n")
          .append("- workoutDescription (string)\n")
          .append("- exercises (array of objects with id (int), sets (int), repetitions (int), weight in kg (int)\n")
          .append("Example:\n{\n  \"workoutName\": \"...\",\n  \"workoutDescription\": \"...\",\n  \"exercises\": [ { \"id\":1, \"sets\":3, \"repetitions\":12, \"weight\":50 }, { \"id\":2, \"sets\":3, \"repetitions\":10, \"weight\":20 } ]\n}");
        return sb.toString();
    }
}