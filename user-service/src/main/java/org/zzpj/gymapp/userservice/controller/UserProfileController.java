package org.zzpj.gymapp.userservice.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zzpj.gymapp.userservice.dto.CreateProfileRequest;
import org.zzpj.gymapp.userservice.dto.UpdateProfileRequest;
import org.zzpj.gymapp.userservice.dto.UserProfileResponse;
import org.zzpj.gymapp.userservice.dto.UserProfileShortResponse;
import org.zzpj.gymapp.userservice.service.UserProfileService;

import java.util.List;

@RestController
@RequestMapping("/profile")
public class UserProfileController {
    private final UserProfileService userProfileService;
    private static final String AUTHORIZATION = "Authorization";

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @PostMapping
    public ResponseEntity<Void> createProfile(@RequestBody @Valid CreateProfileRequest request) {
        userProfileService.createProfile(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileResponse> getProfile(
            @RequestHeader(name = AUTHORIZATION) String authHeader,
            @PathVariable Long userId) {
        UserProfileResponse response = userProfileService.getProfile(authHeader, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUserProfile(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(name = AUTHORIZATION) String authHeader
    ) {
        UserProfileResponse response = userProfileService.getProfile(authHeader, userId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/me")
    public ResponseEntity<UserProfileResponse> updateCurrentUserProfile(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(name = AUTHORIZATION) String authHeader,
            @RequestBody @Valid UpdateProfileRequest updateRequest
    ) {
        UserProfileResponse response = userProfileService.updateProfile(authHeader, userId, updateRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/short")
    public ResponseEntity<UserProfileShortResponse> getProfileShort(@PathVariable Long userId) {
        UserProfileShortResponse response = userProfileService.getProfileShort(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/role/{role}")
    public ResponseEntity<List<UserProfileResponse>> getProfilesByRole(
            @RequestHeader(name = AUTHORIZATION) String authHeader,
            @PathVariable String role,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        List<UserProfileResponse> responses = userProfileService.getProfilesByRole(authHeader, role, page, size);
        return ResponseEntity.ok(responses);
    }
}