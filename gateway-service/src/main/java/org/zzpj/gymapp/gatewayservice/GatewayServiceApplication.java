package org.zzpj.gymapp.gatewayservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
public class GatewayServiceApplication {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()

                .route("user-service", r -> r.path("/api/user/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://user-service"))
                .route("auth-service", r -> r.path("/api/auth/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://auth-service"))
                .route("schedule-service", r -> r.path("/api/schedule/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://schedule-service"))
                .route("workout-generation-service", r -> r.path("/api/workout-generation/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://workout-generation-service"))
                .build();
    }


    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
    }

}
