package org.zzpj.gymapp.userservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zzpj.gymapp.userservice.dto.CreateProfileRequest;
import org.zzpj.gymapp.userservice.entity.UserProfile;
import org.zzpj.gymapp.userservice.repository.UserProfileRepository;

@RestController
@RequestMapping("/profile")
public class UserProfileController {
    private final UserProfileRepository userProfileRepository;

    public UserProfileController(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    @PostMapping
    public ResponseEntity<Void> createProfile(@RequestBody CreateProfileRequest req) {
        UserProfile profile = new UserProfile();
        profile.setUserId(req.userId);
        userProfileRepository.save(profile);
        return ResponseEntity.ok().build();
    }
} 