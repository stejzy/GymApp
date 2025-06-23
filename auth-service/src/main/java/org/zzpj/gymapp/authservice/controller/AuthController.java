package org.zzpj.gymapp.authservice.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.zzpj.gymapp.authservice.dto.LoginRequest;
import org.zzpj.gymapp.authservice.dto.RegisterRequest;
import org.zzpj.gymapp.authservice.entity.Role;
import org.zzpj.gymapp.authservice.service.AuthService;
import org.zzpj.gymapp.authservice.dto.UserInfoDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public Map<String, String> register(@Valid @RequestBody RegisterRequest request) {
        authService.registerUser(request);
        return Map.of("message", "User registered successfully!", "username", request.getUsername());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Please use OAuth2 authorization endpoint");
        response.put("authorization_endpoint", "/oauth2/authorize");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user-info")
    public UserInfoDto getUserInfo(Authentication authentication) {
        return authService.getUserInfo(authentication);
    }

    @GetMapping("/users/role/{role}")
    public List<Long> getUserIdsByRole(
            @PathVariable("role") String role,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        return authService.getUserIdsByRole(role, page, size);
    }

    @GetMapping("/users/{userId}/roles")
    public Set<Role> getUserRolesById(@PathVariable Long userId) {
        return authService.getUserRolesById(userId);
    }
}
