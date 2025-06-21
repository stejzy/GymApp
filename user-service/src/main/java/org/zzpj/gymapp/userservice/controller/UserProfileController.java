package org.zzpj.gymapp.userservice.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.zzpj.gymapp.userservice.dto.CreateProfileRequest;
import org.zzpj.gymapp.userservice.dto.UpdateProfileRequest;
import org.zzpj.gymapp.userservice.dto.UserProfileResponse;
import org.zzpj.gymapp.userservice.dto.UserProfileShortResponse;
import org.zzpj.gymapp.userservice.entity.UserProfile;
import org.zzpj.gymapp.userservice.repository.UserProfileRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import org.springframework.core.ParameterizedTypeReference;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/profile")
public class UserProfileController {
    private final UserProfileRepository userProfileRepository;

    private final WebClient authServiceClient;

    private static final String AUTHORIZATION = "Authorization";

    public UserProfileController(UserProfileRepository userProfileRepository,
                                 WebClient.Builder webClientBuilder,
                                 @Value("${auth.base-url}") String authBaseUrl) {
        this.userProfileRepository = userProfileRepository;
        this.authServiceClient = webClientBuilder.baseUrl(authBaseUrl).build();
    }

    @PostMapping
    public ResponseEntity<Void> createProfile(@RequestBody @Valid CreateProfileRequest req) {
        UserProfile profile = new UserProfile();
        profile.setUserId(req.getUserId());
        userProfileRepository.save(profile);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}")
    public Mono<ResponseEntity<UserProfileResponse>> getProfile(
            @RequestHeader(name = AUTHORIZATION) String authHeader,
            @PathVariable Long userId) {

        Mono<UserProfile> profileMono = Mono.fromCallable(() -> userProfileRepository.findByUserId(userId)
                        .orElseThrow(() -> new RuntimeException("Profile not found")))
                .subscribeOn(Schedulers.boundedElastic());

        Mono<Set<String>> rolesMono = authServiceClient.get()
                .uri("/users/{userId}/roles", userId)
                .header(AUTHORIZATION, authHeader)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {
                });

        return Mono.zip(profileMono, rolesMono)
                .map(tuple -> {
                    UserProfile profile = tuple.getT1();
                    Set<String> roles = tuple.getT2();
                    UserProfileResponse response = new UserProfileResponse(
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
                    return ResponseEntity.ok(response);
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/me")
    public Mono<ResponseEntity<UserProfileResponse>> getCurrentUserProfile(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(name = AUTHORIZATION) String authHeader
    ) {
        return getProfile(authHeader, userId);
    }

    @PatchMapping("/me")
    public Mono<ResponseEntity<UserProfileResponse>> updateCurrentUserProfile(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(name = AUTHORIZATION) String authHeader,
            @RequestBody @Valid UpdateProfileRequest updateDto
    ) {
        return Mono.fromCallable(() -> {
                    UserProfile existing = userProfileRepository.findByUserId(userId)
                            .orElseThrow(() -> new RuntimeException("Profile not found for update"));

                    applyUpdates(existing, updateDto);

                    return userProfileRepository.save(existing);
                }).subscribeOn(Schedulers.boundedElastic())
                .flatMap(updatedProfile -> getProfile(authHeader, updatedProfile.getUserId()));
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

    @GetMapping("/role/{role}")
    public Mono<ResponseEntity<List<UserProfileResponse>>> getProfilesByRole(
            @RequestHeader(name = AUTHORIZATION) String authHeader,
            @PathVariable String role,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        return authServiceClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/users/role/{role}")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build(role.toUpperCase()))
                .header(AUTHORIZATION, authHeader)
                .retrieve()
                .bodyToFlux(Long.class)
                .collectList()
                .publishOn(Schedulers.boundedElastic())
                .flatMap(userIds -> {
                    if (userIds.isEmpty()) {
                        return Mono.just(ResponseEntity.ok(List.of()));
                    }
                    List<UserProfile> profiles = userProfileRepository.findByUserIdIn(userIds);

                    Map<Long, UserProfile> profileMap = profiles.stream()
                            .collect(Collectors.toMap(UserProfile::getUserId, profile -> profile));

                    return Flux.fromIterable(userIds)
                            .flatMap(userId -> {
                                Mono<Set<String>> rolesMono = authServiceClient.get()
                                        .uri("/users/{userId}/roles", userId)
                                        .header(AUTHORIZATION, authHeader)
                                        .retrieve()
                                        .bodyToMono(new ParameterizedTypeReference<Set<String>>() {})
                                        .onErrorReturn(Set.of());

                                return rolesMono.map(roles -> {
                                    UserProfile profile = profileMap.get(userId);
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
                                });
                            })
                            .collectList()
                            .map(ResponseEntity::ok);
                });
    }

}