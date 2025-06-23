package org.zzpj.gymapp.authservice.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.reactive.function.client.WebClient;
import org.zzpj.gymapp.authservice.client.UserClient;

@TestConfiguration
public class TestConfig {

    @MockitoBean
    private WebClient webClient;

    @MockitoBean
    private UserClient userClient;

    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
} 