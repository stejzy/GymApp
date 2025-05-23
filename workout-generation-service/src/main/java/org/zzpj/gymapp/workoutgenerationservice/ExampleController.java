package org.zzpj.gymapp.workoutgenerationservice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class ExampleController {

    private final ExampleService exampleService;

    public ExampleController(ExampleService exampleService){
        this.exampleService = exampleService;
    }

    @GetMapping("/hello")
    public Mono<String> helloWorld(@RequestHeader("Authorization") String authorizationHeader){
        String jwtToken = authorizationHeader.substring(7);

        return exampleService.helloWorld(jwtToken);
    }
}
