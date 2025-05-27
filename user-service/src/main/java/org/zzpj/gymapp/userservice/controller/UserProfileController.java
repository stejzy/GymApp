package org.zzpj.gymapp.userservice.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.zzpj.gymapp.userservice.dto.CreateProfileRequest;
import org.zzpj.gymapp.userservice.dto.UpdateProfileRequest;
import org.zzpj.gymapp.userservice.dto.UserProfileResponse;
import org.zzpj.gymapp.userservice.dto.UserProfileShortResponse;
import org.zzpj.gymapp.userservice.entity.UserProfile;
import org.zzpj.gymapp.userservice.repository.UserProfileRepository;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/profile")
public class UserProfileController {
    private final UserProfileRepository userProfileRepository;

    public UserProfileController(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    @PostMapping
    public ResponseEntity<Void> createProfile(@RequestBody @Valid CreateProfileRequest req) {
        UserProfile profile = new UserProfile();
        profile.setUserId(req.getUserId());
        userProfileRepository.save(profile);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserProfileResponse> getProfile(@PathVariable Long userId) {
        return userProfileRepository.findByUserId(userId)
                .map(profile -> ResponseEntity.ok(
                        new UserProfileResponse(
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
                                profile.getAvatarUrl()
                        )))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUserProfile(
            @RequestHeader("X-User-Id") Long userId
    ) {
        return userProfileRepository.findByUserId(userId)
                .map(profile -> ResponseEntity.ok(
                     new UserProfileResponse(
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
                             profile.getAvatarUrl()
                    )))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/me")
    public ResponseEntity<UserProfileResponse> updateCurrentUserProfile(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody @Valid UpdateProfileRequest updateDto
    ) {
        return userProfileRepository.findByUserId(userId)
                .map(existing -> {
                    if(updateDto.getFirstName() != null) existing.setFirstName(updateDto.getFirstName());
                    if(updateDto.getLastName() != null) existing.setLastName(updateDto.getLastName());
                    if(updateDto.getGender() != null) existing.setGender(updateDto.getGender());
                    if(updateDto.getHeight() != null) existing.setHeight(updateDto.getHeight());
                    if(updateDto.getWeight() != null) existing.setWeight(updateDto.getWeight());
                    if(updateDto.getBirthday() != null) existing.setBirthday(updateDto.getBirthday());
                    if(updateDto.getPhone() != null) existing.setPhone(updateDto.getPhone());
                    if(updateDto.getLevel() != null) existing.setLevel(updateDto.getLevel());
                    if(updateDto.getBio() != null) existing.setBio(updateDto.getBio());
                    if(updateDto.getAvatarUrl() != null) existing.setAvatarUrl(updateDto.getAvatarUrl());

                    userProfileRepository.save(existing);
                    return ResponseEntity.ok(
                            new UserProfileResponse(
                                    existing.getId(),
                                    existing.getUserId(),
                                    existing.getFirstName(),
                                    existing.getLastName(),
                                    existing.getGender(),
                                    existing.getHeight(),
                                    existing.getWeight(),
                                    existing.getBirthday(),
                                    existing.getPhone(),
                                    existing.getLevel(),
                                    existing.getBio(),
                                    existing.getAvatarUrl()
                            ));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    @GetMapping("/{userId}/short")
    public ResponseEntity<UserProfileShortResponse> getProfileShort(@PathVariable Long userId) {
        return userProfileRepository.findByUserId(userId)
                .map(profile -> ResponseEntity.ok(
                        new UserProfileShortResponse(
                            profile.getFirstName(),
                            profile.getLastName()
                    )))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }

}