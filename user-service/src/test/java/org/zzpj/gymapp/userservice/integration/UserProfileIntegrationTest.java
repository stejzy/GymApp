package org.zzpj.gymapp.userservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.zzpj.gymapp.userservice.client.AuthServiceClient;
import org.zzpj.gymapp.userservice.dto.CreateProfileRequest;
import org.zzpj.gymapp.userservice.dto.UpdateProfileRequest;
import org.zzpj.gymapp.userservice.entity.UserProfile;
import org.zzpj.gymapp.userservice.repository.UserProfileRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UserProfileIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @MockitoBean
    private AuthServiceClient authServiceClient;

    private String baseUrl;
    private CreateProfileRequest createRequest;
    private UpdateProfileRequest updateRequest;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/profile";
        userProfileRepository.deleteAll();

        createRequest = new CreateProfileRequest(1L);
        createRequest.setFirstName("John");
        createRequest.setLastName("Doe");
        createRequest.setGender("Male");
        createRequest.setHeight(180.0);
        createRequest.setWeight(75.0);
        createRequest.setBirthday(LocalDate.of(1990, 1, 1));
        createRequest.setPhone("+48123456789");
        createRequest.setBio("Test bio");
        createRequest.setAvatarUrl("https://example.com/avatar.jpg");

        updateRequest = new UpdateProfileRequest();
        updateRequest.setFirstName("Jane");
        updateRequest.setLastName("Smith");

        // Mock AuthServiceClient responses
        when(authServiceClient.getUserRoles(anyLong(), anyString()))
                .thenReturn(Set.of("MEMBER"));
        when(authServiceClient.getUserIdsByRole(anyString(), anyInt(), anyInt(), anyString()))
                .thenReturn(List.of(1L, 2L));
    }

    @Test
    void shouldCreateProfileSuccessfully() {
        // When
        ResponseEntity<Void> response = restTemplate.postForEntity(
                baseUrl,
                createRequest,
                Void.class
        );

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(userProfileRepository.existsByUserId(1L)).isTrue();
    }

    @Test
    void shouldReturn409WhenCreatingDuplicateProfile() {
        // Given
        userProfileRepository.save(createUserProfile(1L));

        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl,
                createRequest,
                Map.class
        );

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(409);
        assertThat(response.getBody().get("title")).isEqualTo("User profile already exists");
    }

    @Test
    void shouldGetProfileSuccessfully() {
        // Given
        UserProfile profile = userProfileRepository.save(createUserProfile(1L));

        // When
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/" + profile.getUserId(),
                HttpMethod.GET,
                createHttpEntityWithAuth(),
                Map.class
        );

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody().get("id")).isEqualTo(profile.getId().intValue());
        assertThat(response.getBody().get("userId")).isEqualTo(profile.getUserId().intValue());
        assertThat(response.getBody().get("firstName")).isEqualTo("John");
        assertThat(response.getBody().get("lastName")).isEqualTo("Doe");
    }

    @Test
    void shouldReturn404WhenProfileNotFound() {
        // When
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/999",
                HttpMethod.GET,
                createHttpEntityWithAuth(),
                Map.class
        );

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(404);
        assertThat(response.getBody().get("title")).isEqualTo("User profile not found");
    }

    @Test
    void shouldGetCurrentUserProfileSuccessfully() {
        // Given
        UserProfile profile = userProfileRepository.save(createUserProfile(1L));

        // When
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/me",
                HttpMethod.GET,
                createHttpEntityWithAuthAndUserId(profile.getUserId()),
                Map.class
        );

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody().get("id")).isEqualTo(profile.getId().intValue());
        assertThat(response.getBody().get("firstName")).isEqualTo("John");
    }

    @Test
    void shouldUpdateCurrentUserProfileSuccessfully() {
        // Given
        UserProfile profile = userProfileRepository.save(createUserProfile(1L));

        // When
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/me",
                HttpMethod.PATCH,
                createHttpEntityWithAuthAndUserId(profile.getUserId(), updateRequest),
                Map.class
        );

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody().get("firstName")).isEqualTo("Jane");
        assertThat(response.getBody().get("lastName")).isEqualTo("Smith");

        // Verify database was updated
        UserProfile updatedProfile = userProfileRepository.findByUserId(1L).orElse(null);
        assertThat(updatedProfile).isNotNull();
        assertThat(updatedProfile.getFirstName()).isEqualTo("Jane");
        assertThat(updatedProfile.getLastName()).isEqualTo("Smith");
    }

    @Test
    void shouldGetProfileShortSuccessfully() {
        // Given
        UserProfile profile = userProfileRepository.save(createUserProfile(1L));

        // When
        ResponseEntity<Map> response = restTemplate.getForEntity(
                baseUrl + "/" + profile.getUserId() + "/short",
                Map.class
        );

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody().get("firstName")).isEqualTo("John");
        assertThat(response.getBody().get("lastName")).isEqualTo("Doe");
    }

    @Test
    void shouldGetProfilesByRoleSuccessfully() {
        // Given
        UserProfile profile1 = userProfileRepository.save(createUserProfile(1L));
        UserProfile profile2 = userProfileRepository.save(createUserProfile(2L));

        // When
        ResponseEntity<List> response = restTemplate.exchange(
                baseUrl + "/role/MEMBER?page=0&size=20",
                HttpMethod.GET,
                createHttpEntityWithAuth(),
                List.class
        );

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(2);
    }

    @Test
    void shouldReturn400ForInvalidCreateRequest() {
        // Given
        CreateProfileRequest invalidRequest = new CreateProfileRequest(1L);
        invalidRequest.setFirstName("A"); // Too short

        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl,
                invalidRequest,
                Map.class
        );

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        assertThat(response.getBody().get("title")).isEqualTo("Validation failed");
    }

    @Test
    void shouldReturn400ForInvalidUpdateRequest() {
        // Given
        UserProfile profile = userProfileRepository.save(createUserProfile(1L));
        UpdateProfileRequest invalidRequest = new UpdateProfileRequest();
        invalidRequest.setPhone("invalid-phone"); // Invalid phone format

        // When
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/me",
                HttpMethod.PATCH,
                createHttpEntityWithAuthAndUserId(profile.getUserId(), invalidRequest),
                Map.class
        );

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        assertThat(response.getBody().get("title")).isEqualTo("Validation failed");
    }

    private UserProfile createUserProfile(Long userId) {
        UserProfile profile = new UserProfile();
        profile.setUserId(userId);
        profile.setFirstName("John");
        profile.setLastName("Doe");
        profile.setGender("Male");
        profile.setHeight(180.0);
        profile.setWeight(75.0);
        profile.setBirthday(LocalDate.of(1990, 1, 1));
        profile.setPhone("+48123456789");
        profile.setBio("Test bio");
        profile.setAvatarUrl("https://example.com/avatar.jpg");
        return profile;
    }

    private HttpEntity<Void> createHttpEntityWithAuth() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer test-token");
        return new HttpEntity<>(headers);
    }

    private HttpEntity<Void> createHttpEntityWithAuthAndUserId(Long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer test-token");
        headers.set("X-User-Id", userId.toString());
        return new HttpEntity<>(headers);
    }

    private HttpEntity<UpdateProfileRequest> createHttpEntityWithAuthAndUserId(Long userId, UpdateProfileRequest body) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer test-token");
        headers.set("X-User-Id", userId.toString());
        headers.set("Content-Type", "application/json");
        return new HttpEntity<>(body, headers);
    }
} 