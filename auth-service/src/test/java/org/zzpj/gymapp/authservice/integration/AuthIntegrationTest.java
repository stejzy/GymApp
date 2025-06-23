package org.zzpj.gymapp.authservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.zzpj.gymapp.authservice.client.UserClient;
import org.zzpj.gymapp.authservice.dto.RegisterRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.zzpj.gymapp.authservice.entity.User;
import org.zzpj.gymapp.authservice.entity.Role;
import org.zzpj.gymapp.authservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;

import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private UserClient  userClient;

    @BeforeEach
    void setUp() {

    }

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        userRepository.deleteAll();

        doNothing().when(userClient).createProfile(Map.of("userId", 1L));

        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("testuser@example.com");
        request.setPassword("password123");
        request.setRole("MEMBER");

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully!"))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"MEMBER"})
    void shouldReturnUserInfoForAuthenticatedUser() throws Exception {
        if (!userRepository.existsByUsername("testuser")) {
            User user = new User();
            user.setUsername("testuser");
            user.setEmail("testuser@example.com");
            user.setPassword(passwordEncoder.encode("password123"));
            user.setRoles(Set.of(Role.MEMBER));
            userRepository.save(user);
        }
        mockMvc.perform(get("/user-info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.roles").isArray());
    }

    @Test
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/user-info"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn400ForInvalidRegisterRequest() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("a");
        request.setEmail("not-an-email");
        request.setPassword("123");
        request.setRole("XD");

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.detail").exists());
    }
} 