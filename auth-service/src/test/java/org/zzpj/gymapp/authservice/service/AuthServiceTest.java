package org.zzpj.gymapp.authservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.zzpj.gymapp.authservice.dto.RegisterRequest;
import org.zzpj.gymapp.authservice.dto.UserInfoDto;
import org.zzpj.gymapp.authservice.entity.Role;
import org.zzpj.gymapp.authservice.entity.User;
import org.zzpj.gymapp.authservice.exception.UnauthorizedException;
import org.zzpj.gymapp.authservice.exception.UserAlreadyExistsException;
import org.zzpj.gymapp.authservice.exception.UserNotFoundException;
import org.zzpj.gymapp.authservice.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setRole("MEMBER");

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRoles(Set.of(Role.MEMBER));
    }

    @Test
    void registerUser_shouldThrowException_whenUsernameExists() {
        // given
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.registerUser(registerRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("Username is already taken!");
        
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_shouldThrowException_whenEmailExists() {
        // given
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.registerUser(registerRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("Email is already in use!");
        
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_shouldCreateCoachRole_whenRoleIsCoach() {
        // given
        registerRequest.setRole("COACH");
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // when & then - expect exception due to WebClient call, but verify user was saved with COACH role
        assertThatThrownBy(() -> authService.registerUser(registerRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User registered, but failed to create profile");
        
        verify(userRepository).save(argThat(user -> user.getRoles().contains(Role.COACH)));
    }

    @Test
    void getUserInfo_shouldReturnUserInfo_whenUserExists() {
        // given
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // when
        UserInfoDto result = authService.getUserInfo(authentication);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getRoles()).contains(Role.MEMBER);
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void getUserInfo_shouldThrowException_whenAuthenticationIsNull() {
        // when & then
        assertThatThrownBy(() -> authService.getUserInfo(null))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void getUserInfo_shouldThrowException_whenUserNotFound() {
        // given
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("nonexistent");
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.getUserInfo(authentication))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void getUserIdsByRole_shouldReturnUserIds() {
        // given
        List<Long> userIds = List.of(1L, 2L, 3L);
        Pageable pageable = PageRequest.of(0, 20);
        when(userRepository.findUserIdsByRole(Role.MEMBER, pageable)).thenReturn(userIds);

        // when
        List<Long> result = authService.getUserIdsByRole("MEMBER", 0, 20);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result).containsExactly(1L, 2L, 3L);
        verify(userRepository).findUserIdsByRole(Role.MEMBER, pageable);
    }

    @Test
    void getUserIdsByRole_shouldThrowException_whenInvalidRole() {
        // when & then
        assertThatThrownBy(() -> authService.getUserIdsByRole("INVALID_ROLE", 0, 20))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid role: INVALID_ROLE");
    }

    @Test
    void getUserRolesById_shouldReturnRoles_whenUserExists() {
        // given
        Set<Role> roles = Set.of(Role.MEMBER, Role.COACH);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        testUser.setRoles(roles);

        // when
        Set<Role> result = authService.getUserRolesById(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).contains(Role.MEMBER, Role.COACH);
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserRolesById_shouldThrowException_whenUserNotFound() {
        // given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.getUserRolesById(999L))
                .isInstanceOf(UserNotFoundException.class);
    }
} 