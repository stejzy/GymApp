package org.zzpj.gymapp.userservice.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.zzpj.gymapp.userservice.client.AuthServiceClient;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public AuthServiceClient authServiceClient() {
        AuthServiceClient mockClient = mock(AuthServiceClient.class);
        
        // Default mock responses
        when(mockClient.getUserRoles(anyLong(), anyString()))
                .thenReturn(Set.of("MEMBER"));
        when(mockClient.getUserIdsByRole(anyString(), anyInt(), anyInt(), anyString()))
                .thenReturn(List.of(1L, 2L));
        
        return mockClient;
    }
} 