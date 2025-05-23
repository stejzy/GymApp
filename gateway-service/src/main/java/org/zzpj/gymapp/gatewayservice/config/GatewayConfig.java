package org.zzpj.gymapp.gatewayservice.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zzpj.gymapp.gatewayservice.service.JwtAuthenticationGatewayFilterFactory;

@Configuration
public class GatewayConfig {
    private final JwtAuthenticationGatewayFilterFactory jwtAuthFilter;

    public GatewayConfig(JwtAuthenticationGatewayFilterFactory jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", r -> r.path("/api/auth/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .filter(jwtAuthFilter.apply(new JwtAuthenticationGatewayFilterFactory.Config())))
                        .uri("lb://auth-service"))

                .route("user-service", r -> r.path("/api/user/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .filter(jwtAuthFilter.apply(new JwtAuthenticationGatewayFilterFactory.Config())))
                        .uri("lb://user-service"))

                .route("schedule-service", r -> r.path("/api/schedule/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .filter(jwtAuthFilter.apply(new JwtAuthenticationGatewayFilterFactory.Config())))
                        .uri("lb://schedule-service"))

                .route("workout-generation-service", r -> r.path("/api/workout-generation/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .filter(jwtAuthFilter.apply(new JwtAuthenticationGatewayFilterFactory.Config())))
                        .uri("lb://workout-generation-service"))

                .build();
    }
}
