package org.zzpj.gymapp.workoutgenerationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class WorkoutGenerationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkoutGenerationServiceApplication.class, args);
    }

}
