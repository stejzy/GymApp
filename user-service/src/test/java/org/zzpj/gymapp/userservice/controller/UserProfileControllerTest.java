package org.zzpj.gymapp.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.zzpj.gymapp.userservice.dto.CreateProfileRequest;
import org.zzpj.gymapp.userservice.dto.UpdateProfileRequest;
import org.zzpj.gymapp.userservice.dto.UserProfileResponse;
import org.zzpj.gymapp.userservice.dto.UserProfileShortResponse;
import org.zzpj.gymapp.userservice.service.UserProfileService;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserProfileController.class)
class UserProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserProfileService userProfileService;

    private CreateProfileRequest createRequest;
    private UpdateProfileRequest updateRequest;
    private UserProfileResponse userProfileResponse;
    private UserProfileShortResponse userProfileShortResponse;

    @BeforeEach
    void setUp() {
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

        userProfileResponse = new UserProfileResponse(
                1L, 1L, "John", "Doe", "Male", 180.0, 75.0,
                LocalDate.of(1990, 1, 1), "+48123456789", null, "Test bio",
                "https://example.com/avatar.jpg", Set.of("MEMBER")
        );

        userProfileShortResponse = new UserProfileShortResponse("John", "Doe");
    }

    @Test
    void shouldCreateProfileSuccessfully() throws Exception {
        mockMvc.perform(post("/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetProfileSuccessfully() throws Exception {
        when(userProfileService.getProfile(anyString(), eq(1L)))
                .thenReturn(userProfileResponse);

        mockMvc.perform(get("/profile/1")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.roles[0]").value("MEMBER"));
    }

    @Test
    void shouldGetCurrentUserProfileSuccessfully() throws Exception {
        when(userProfileService.getProfile(anyString(), eq(1L)))
                .thenReturn(userProfileResponse);

        mockMvc.perform(get("/profile/me")
                        .header("X-User-Id", "1")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void shouldUpdateCurrentUserProfileSuccessfully() throws Exception {
        when(userProfileService.updateProfile(anyString(), eq(1L), any(UpdateProfileRequest.class)))
                .thenReturn(userProfileResponse);

        mockMvc.perform(patch("/profile/me")
                        .header("X-User-Id", "1")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void shouldGetProfileShortSuccessfully() throws Exception {
        when(userProfileService.getProfileShort(1L))
                .thenReturn(userProfileShortResponse);

        mockMvc.perform(get("/profile/1/short"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    void shouldGetProfilesByRoleSuccessfully() throws Exception {
        List<UserProfileResponse> profiles = List.of(userProfileResponse);
        when(userProfileService.getProfilesByRole(anyString(), eq("MEMBER"), eq(0), eq(20)))
                .thenReturn(profiles);

        mockMvc.perform(get("/profile/role/MEMBER")
                        .header("Authorization", "Bearer token")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("John"));
    }

    @Test
    void shouldReturn400ForInvalidCreateRequest() throws Exception {
        CreateProfileRequest invalidRequest = new CreateProfileRequest(1L);
        invalidRequest.setFirstName("A"); // Too short

        mockMvc.perform(post("/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"));
    }

    @Test
    void shouldReturn400ForInvalidUpdateRequest() throws Exception {
        UpdateProfileRequest invalidRequest = new UpdateProfileRequest();
        invalidRequest.setPhone("invalid-phone"); // Invalid phone format

        mockMvc.perform(patch("/profile/me")
                        .header("X-User-Id", "1")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"));
    }
} 