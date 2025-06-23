package org.zzpj.gymapp.authservice.service;

import feign.FeignException;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.zzpj.gymapp.authservice.client.UserClient;
import org.zzpj.gymapp.authservice.dto.RegisterRequest;
import org.zzpj.gymapp.authservice.entity.Role;
import org.zzpj.gymapp.authservice.entity.User;
import org.zzpj.gymapp.authservice.exception.ProfileCreationException;
import org.zzpj.gymapp.authservice.repository.UserRepository;
import org.zzpj.gymapp.authservice.dto.UserInfoDto;
import org.zzpj.gymapp.authservice.exception.UserNotFoundException;
import org.zzpj.gymapp.authservice.exception.UnauthorizedException;
import org.zzpj.gymapp.authservice.exception.UserAlreadyExistsException;

import java.util.Map;
import java.util.Set;
import java.util.List;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserClient userClient;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserClient userClient) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userClient = userClient;
    }

    public void registerUser(@Valid RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username is already taken!");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email is already in use!");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        Role role = request.getRole().equalsIgnoreCase("COACH") ? Role.COACH : Role.MEMBER;
        user.setRoles(Set.of(role));

        userRepository.save(user);

        try {
            userClient.createProfile(Map.of("userId", user.getId()));
        } catch (FeignException ex) {
            userRepository.delete(user);
            throw new ProfileCreationException("User registered, but failed to create profile: " + ex.contentUTF8(), ex);
        } catch (Exception ex) {
            userRepository.delete(user);
            throw new ProfileCreationException("User registered, but failed to create profile: " + ex.getMessage(), ex);
        }
    }

    public UserInfoDto getUserInfo(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException();
        }
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(UserNotFoundException::new);
        return new UserInfoDto(user.getId(), user.getUsername(), user.getRoles(), authentication.getAuthorities());
    }

    public List<Long> getUserIdsByRole(String role, int page, int size) {
        Role roleEnum;
        try {
            roleEnum = Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findUserIdsByRole(roleEnum, pageable);
    }

    public Set<Role> getUserRolesById(Long userId) {
        return userRepository.findById(userId)
                .map(User::getRoles)
                .orElseThrow(UserNotFoundException::new);
    }
}
