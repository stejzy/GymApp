package org.zzpj.gymapp.gatewayservice.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Component
public class AuthHealthIndicator implements HealthIndicator {

    @Value("${auth.base-url}")
    private String authBaseUrl;

    private final WebClient webClient;

    public AuthHealthIndicator() {
        this.webClient = WebClient.builder()
                .baseUrl(authBaseUrl)
                .build();
    }

    @Override
    public Health health() {
        try {
            webClient.get()
                    .uri("/actuator/health")
                    .retrieve()
                    .toBodilessEntity()
                    .timeout(Duration.ofSeconds(15))
                    .block();

            return Health.up()
                    .withDetail("auth-service", "Available")
                    .build();

        } catch (Exception e) {
            return Health.down()
                    .withDetail("auth-service", "Unavailable")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}