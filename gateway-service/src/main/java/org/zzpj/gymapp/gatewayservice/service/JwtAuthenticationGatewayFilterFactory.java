package org.zzpj.gymapp.gatewayservice.service;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationGatewayFilterFactory extends AbstractGatewayFilterFactory<JwtAuthenticationGatewayFilterFactory.Config> {
    private final JwtDecoder jwtDecoder;

    public JwtAuthenticationGatewayFilterFactory(JwtDecoder jwtDecoder) {
        super(Config.class);
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().value();

            if (path.startsWith("/oauth2/") ||
                path.equals("/register") ||
                path.equals("/login") ||
                path.equals("/default-ui.css") ||
                path.equals("/")) {
                return chain.filter(exchange);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Missing or invalid Authorization Header", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            try {
                Jwt jwt = jwtDecoder.decode(token);

                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-User-Id", jwt.getClaimAsString("user_id"))
                        .header("X-User-Username", jwt.getClaimAsString("sub"))
                        .header("X-User-Email", jwt.getClaimAsString("email"))
                        .header("X-User-Roles", jwt.getClaimAsString("roles"))
                        .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            } catch (JwtException e) {
                return onError(exchange, "Invalid JWT token", HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

    public static class Config {

    }
}
