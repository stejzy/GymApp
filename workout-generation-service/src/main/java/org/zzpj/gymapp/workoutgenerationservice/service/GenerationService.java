package org.zzpj.gymapp.workoutgenerationservice.service;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import org.springframework.stereotype.Service;

@Service
public class GenerationService {
    private final OpenAIClient openAIClient;

    public GenerationService() {
        // Klucz API powinien być ustawiony w zmiennej środowiskowej OPENAI_API_KEY
        this.openAIClient = OpenAIOkHttpClient.fromEnv();
    }

    public String generateText(String prompt) {
        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .addUserMessage(prompt)
                .model(ChatModel.GPT_4_1_NANO_2025_04_14)
                .build();
        ChatCompletion chatCompletion = openAIClient.chat().completions().create(params);
        return chatCompletion.choices().getFirst().message().content().orElse("");
    }
} 