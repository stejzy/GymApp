package org.zzpj.gymapp.workoutgenerationservice;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class ExampleService {

    private final WebClient webClient;

    public ExampleService(WebClient.Builder webClientBuilder){
        this.webClient = webClientBuilder.build();
    }

    public Mono<String> helloWorld(){
        return webClient.get()
                .uri("http://user-service/api/helloWorld")
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(e -> System.out.println("Błąd"));
    }

}
