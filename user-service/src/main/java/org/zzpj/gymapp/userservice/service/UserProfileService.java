package org.zzpj.gymapp.userservice.service;

import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.zzpj.gymapp.userservice.client.AuthServiceClient;
import org.zzpj.gymapp.userservice.dto.CreateProfileRequest;
import org.zzpj.gymapp.userservice.dto.UpdateProfileRequest;
import org.zzpj.gymapp.userservice.dto.UserProfileResponse;
import org.zzpj.gymapp.userservice.dto.UserProfileShortResponse;
import org.zzpj.gymapp.userservice.entity.UserProfile;
import org.zzpj.gymapp.userservice.exception.UserProfileAlreadyExistsException;
import org.zzpj.gymapp.userservice.exception.UserProfileNotFoundException;
import org.zzpj.gymapp.userservice.repository.UserProfileRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserProfileService {
    private final UserProfileRepository userProfileRepository;
    private final AuthServiceClient authServiceClient;

    public UserProfileService(UserProfileRepository userProfileRepository,
                             AuthServiceClient authServiceClient) {
        this.userProfileRepository = userProfileRepository;
        this.authServiceClient = authServiceClient;
    }

    public void createProfile(@Valid CreateProfileRequest request) {
        if (userProfileRepository.existsByUserId(request.getUserId())) {
            throw new UserProfileAlreadyExistsException("Profile already exists for user ID: " + request.getUserId());
        }

        UserProfile profile = new UserProfile();
        profile.setUserId(request.getUserId());
        userProfileRepository.save(profile);
    }

    public UserProfileResponse getProfile(String authHeader, Long userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new UserProfileNotFoundException("Profile not found for user ID: " + userId));

        Set<String> roles = authServiceClient.getUserRoles(userId, authHeader);

        return new UserProfileResponse(
                profile.getId(),
                profile.getUserId(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getGender(),
                profile.getHeight(),
                profile.getWeight(),
                profile.getBirthday(),
                profile.getPhone(),
                profile.getLevel(),
                profile.getBio(),
                profile.getAvatarUrl(),
                roles
        );
    }

    public UserProfileResponse updateProfile(String authHeader, Long userId, @Valid UpdateProfileRequest updateRequest) {
        UserProfile existing = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new UserProfileNotFoundException("Profile not found for user ID: " + userId));

        applyUpdates(existing, updateRequest);
        UserProfile updatedProfile = userProfileRepository.save(existing);

        return getProfile(authHeader, updatedProfile.getUserId());
    }

    public UserProfileShortResponse getProfileShort(Long userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new UserProfileNotFoundException("Profile not found for user ID: " + userId));

        return new UserProfileShortResponse(
                profile.getFirstName(),
                profile.getLastName()
        );
    }

    public List<UserProfileResponse> getProfilesByRole(String authHeader, String role, int page, int size) {
        List<Long> userIds = authServiceClient.getUserIdsByRole(role.toUpperCase(), page, size, authHeader);

        if (userIds.isEmpty()) {
            return List.of();
        }

        List<UserProfile> profiles = userProfileRepository.findByUserIdIn(userIds);
        Map<Long, UserProfile> profileMap = profiles.stream()
                .collect(Collectors.toMap(UserProfile::getUserId, profile -> profile));

        return userIds.stream()
                .map(userId -> {
                    UserProfile profile = profileMap.get(userId);
                    Set<String> roles = authServiceClient.getUserRoles(userId, authHeader);
                    
                    return new UserProfileResponse(
                            profile.getId(),
                            profile.getUserId(),
                            profile.getFirstName(),
                            profile.getLastName(),
                            profile.getGender(),
                            profile.getHeight(),
                            profile.getWeight(),
                            profile.getBirthday(),
                            profile.getPhone(),
                            profile.getLevel(),
                            profile.getBio(),
                            profile.getAvatarUrl(),
                            roles
                    );
                })
                .collect(Collectors.toList());
    }

    private void applyUpdates(UserProfile profile, UpdateProfileRequest update) {
        Optional.ofNullable(update.getFirstName()).ifPresent(profile::setFirstName);
        Optional.ofNullable(update.getLastName()).ifPresent(profile::setLastName);
        Optional.ofNullable(update.getGender()).ifPresent(profile::setGender);
        Optional.ofNullable(update.getHeight()).ifPresent(profile::setHeight);
        Optional.ofNullable(update.getWeight()).ifPresent(profile::setWeight);
        Optional.ofNullable(update.getBirthday()).ifPresent(profile::setBirthday);
        Optional.ofNullable(update.getPhone()).ifPresent(profile::setPhone);
        Optional.ofNullable(update.getLevel()).ifPresent(profile::setLevel);
        Optional.ofNullable(update.getBio()).ifPresent(profile::setBio);
        Optional.ofNullable(update.getAvatarUrl()).ifPresent(profile::setAvatarUrl);
    }
} 