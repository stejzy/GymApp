package org.zzpj.gymapp.authservice.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.zzpj.gymapp.authservice.dto.LoginRequest;
import org.zzpj.gymapp.authservice.dto.RegisterRequest;
import org.zzpj.gymapp.authservice.entity.Role;
import org.zzpj.gymapp.authservice.entity.User;
import org.zzpj.gymapp.authservice.repository.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
public class AuthController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final WebClient webClient;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, @Value("${user.base-url}") String userBaseUrl) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.webClient = WebClient.builder()
                .baseUrl(userBaseUrl)
                .build();
    }

    @PostMapping("/register")
    public ResponseEntity<?> register (@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Username is already taken!"));
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Email is already in use!"));
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        Role role = request.getRole().equalsIgnoreCase("COACH") ? Role.COACH : Role.MEMBER;
        user.setRoles(Set.of(role));

        userRepository.save(user);

        try {
            webClient.post()
                .uri("/profile")
                .bodyValue(Map.of("userId", user.getId()))
                .retrieve()
                .bodyToMono(Void.class)
                .block();
        } catch (WebClientResponseException ex) {
            userRepository.delete(user);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "User registered, but failed to create profile: " + ex.getResponseBodyAsString()));
        } catch (Exception ex) {
            userRepository.delete(user);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "User registered, but failed to create profile: " + ex.getMessage()));
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", "User registered successfuly!");
        response.put("username", user.getUsername());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Please use OAuth2 authorization endpoint");
        response.put("authorization_endpoint", "/oauth2/authorize");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user-info")
    public ResponseEntity<?> getUserInfo(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("roles", user.getRoles());
        userInfo.put("authorities", authentication.getAuthorities());

        return ResponseEntity.ok(userInfo);
    }

    @GetMapping("/users/role/{role}")
    public ResponseEntity<List<Long>> getUserIdsByRole(
            @PathVariable("role") String role,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        Role roleEnum;
        try {
            roleEnum = Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
        Pageable pageable = PageRequest.of(page, size);
        List<Long> userIds = userRepository.findUserIdsByRole(roleEnum, pageable);
        return ResponseEntity.ok(userIds);
    }

    @GetMapping("/users/{userId}/roles")
    public ResponseEntity<Set<Role>> getUserRolesById(@PathVariable Long userId) {
        return userRepository.findById(userId)
                .map(user -> ResponseEntity.ok(user.getRoles()))
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
