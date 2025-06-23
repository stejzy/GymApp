package org.zzpj.gymapp.userservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zzpj.gymapp.userservice.client.AuthServiceClient;
import org.zzpj.gymapp.userservice.dto.CreateProfileRequest;
import org.zzpj.gymapp.userservice.dto.UpdateProfileRequest;
import org.zzpj.gymapp.userservice.dto.UserProfileResponse;
import org.zzpj.gymapp.userservice.dto.UserProfileShortResponse;
import org.zzpj.gymapp.userservice.entity.UserProfile;
import org.zzpj.gymapp.userservice.exception.UserProfileAlreadyExistsException;
import org.zzpj.gymapp.userservice.exception.UserProfileNotFoundException;
import org.zzpj.gymapp.userservice.repository.UserProfileRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private AuthServiceClient authServiceClient;

    @InjectMocks
    private UserProfileService userProfileService;

    private CreateProfileRequest createRequest;
    private UpdateProfileRequest updateRequest;
    private UserProfile userProfile;

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

        userProfile = new UserProfile();
        userProfile.setId(1L);
        userProfile.setUserId(1L);
        userProfile.setFirstName("John");
        userProfile.setLastName("Doe");
        userProfile.setGender("Male");
        userProfile.setHeight(180.0);
        userProfile.setWeight(75.0);
        userProfile.setBirthday(LocalDate.of(1990, 1, 1));
        userProfile.setPhone("+48123456789");
        userProfile.setBio("Test bio");
        userProfile.setAvatarUrl("https://example.com/avatar.jpg");
    }

    @Test
    void shouldCreateProfileSuccessfully() {
        // Given
        when(userProfileRepository.existsByUserId(1L)).thenReturn(false);
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(userProfile);

        // When
        userProfileService.createProfile(createRequest);

        // Then
        verify(userProfileRepository).existsByUserId(1L);
        verify(userProfileRepository).save(any(UserProfile.class));
    }

    @Test
    void shouldThrowExceptionWhenProfileAlreadyExists() {
        // Given
        when(userProfileRepository.existsByUserId(1L)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userProfileService.createProfile(createRequest))
                .isInstanceOf(UserProfileAlreadyExistsException.class)
                .hasMessage("Profile already exists for user ID: 1");

        verify(userProfileRepository).existsByUserId(1L);
        verify(userProfileRepository, never()).save(any(UserProfile.class));
    }

    @Test
    void shouldGetProfileSuccessfully() {
        // Given
        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.of(userProfile));
        when(authServiceClient.getUserRoles(1L, "Bearer token")).thenReturn(Set.of("MEMBER"));

        // When
        UserProfileResponse response = userProfileService.getProfile("Bearer token", 1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getFirstName()).isEqualTo("John");
        assertThat(response.getLastName()).isEqualTo("Doe");
        assertThat(response.getRoles()).contains("MEMBER");

        verify(userProfileRepository).findByUserId(1L);
        verify(authServiceClient).getUserRoles(1L, "Bearer token");
    }

    @Test
    void shouldThrowExceptionWhenProfileNotFound() {
        // Given
        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userProfileService.getProfile("Bearer token", 1L))
                .isInstanceOf(UserProfileNotFoundException.class)
                .hasMessage("Profile not found for user ID: 1");

        verify(userProfileRepository).findByUserId(1L);
        verify(authServiceClient, never()).getUserRoles(anyLong(), anyString());
    }

    @Test
    void shouldUpdateProfileSuccessfully() {
        // Given
        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.of(userProfile));
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(userProfile);
        when(authServiceClient.getUserRoles(1L, "Bearer token")).thenReturn(Set.of("MEMBER"));

        // When
        UserProfileResponse response = userProfileService.updateProfile("Bearer token", 1L, updateRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getFirstName()).isEqualTo("Jane");
        verify(userProfileRepository, times(2)).findByUserId(1L);
        verify(userProfileRepository).save(any(UserProfile.class));
        verify(authServiceClient).getUserRoles(1L, "Bearer token");
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentProfile() {
        // Given
        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userProfileService.updateProfile("Bearer token", 1L, updateRequest))
                .isInstanceOf(UserProfileNotFoundException.class)
                .hasMessage("Profile not found for user ID: 1");

        verify(userProfileRepository).findByUserId(1L);
        verify(userProfileRepository, never()).save(any(UserProfile.class));
    }

    @Test
    void shouldGetProfileShortSuccessfully() {
        // Given
        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.of(userProfile));

        // When
        UserProfileShortResponse response = userProfileService.getProfileShort(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getFirstName()).isEqualTo("John");
        assertThat(response.getLastName()).isEqualTo("Doe");

        verify(userProfileRepository).findByUserId(1L);
    }

    @Test
    void shouldThrowExceptionWhenGettingShortProfileNotFound() {
        // Given
        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userProfileService.getProfileShort(1L))
                .isInstanceOf(UserProfileNotFoundException.class)
                .hasMessage("Profile not found for user ID: 1");

        verify(userProfileRepository).findByUserId(1L);
    }

    @Test
    void shouldGetProfilesByRoleSuccessfully() {
        // Given
        List<Long> userIds = List.of(1L, 2L);

        UserProfile userProfile1 = new UserProfile();
        userProfile1.setId(1L);
        userProfile1.setUserId(1L);
        userProfile1.setFirstName("Big");

        UserProfile userProfile2 = new UserProfile();
        userProfile2.setId(2L);
        userProfile2.setUserId(2L);
        userProfile2.setFirstName("Huge");

        List<UserProfile> profiles = List.of(userProfile1, userProfile2);

        when(authServiceClient.getUserIdsByRole("MEMBER", 0, 20, "Bearer token")).thenReturn(userIds);
        when(userProfileRepository.findByUserIdIn(userIds)).thenReturn(profiles);
        when(authServiceClient.getUserRoles(1L, "Bearer token")).thenReturn(Set.of("MEMBER"));

        // When
        List<UserProfileResponse> responses = userProfileService.getProfilesByRole("Bearer token", "MEMBER", 0, 20);

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.getFirst().getFirstName()).isEqualTo("Big");
        assertThat(responses.get(1).getFirstName()).isEqualTo("Huge");

        verify(authServiceClient).getUserIdsByRole("MEMBER", 0, 20, "Bearer token");
        verify(userProfileRepository).findByUserIdIn(userIds);
        verify(authServiceClient, times(2)).getUserRoles(anyLong(), eq("Bearer token"));
    }

    @Test
    void shouldReturnEmptyListWhenNoUsersWithRole() {
        // Given
        when(authServiceClient.getUserIdsByRole("MEMBER", 0, 20, "Bearer token")).thenReturn(List.of());

        // When
        List<UserProfileResponse> responses = userProfileService.getProfilesByRole("Bearer token", "MEMBER", 0, 20);

        // Then
        assertThat(responses).isEmpty();

        verify(authServiceClient).getUserIdsByRole("MEMBER", 0, 20, "Bearer token");
        verify(userProfileRepository, never()).findByUserIdIn(anyList());
        verify(authServiceClient, never()).getUserRoles(anyLong(), anyString());
    }

    @Test
    void shouldApplyUpdatesCorrectly() {
        // Given
        UserProfile profile = new UserProfile();
        profile.setUserId(1L);
        profile.setFirstName("Original");
        profile.setLastName("Name");

        UpdateProfileRequest update = new UpdateProfileRequest();
        update.setFirstName("Updated");
        update.setLastName("Surname");

        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(profile);
        when(authServiceClient.getUserRoles(1L, "Bearer token")).thenReturn(Set.of("MEMBER"));

        // When
        userProfileService.updateProfile("Bearer token", 1L, update);

        // Then
        verify(userProfileRepository).save(profile);
        assertThat(profile.getFirstName()).isEqualTo("Updated");
        assertThat(profile.getLastName()).isEqualTo("Surname");
    }
} 