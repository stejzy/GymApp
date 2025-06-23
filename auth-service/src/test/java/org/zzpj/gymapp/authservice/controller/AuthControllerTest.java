package org.zzpj.gymapp.authservice.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.zzpj.gymapp.authservice.dto.LoginRequest;
import org.zzpj.gymapp.authservice.dto.RegisterRequest;
import org.zzpj.gymapp.authservice.dto.UserInfoDto;
import org.zzpj.gymapp.authservice.entity.Role;
import org.zzpj.gymapp.authservice.entity.User;
import org.zzpj.gymapp.authservice.service.AuthService;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthController authController;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User testUser;
    private UserInfoDto userInfoDto;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setRole("MEMBER");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setRoles(Set.of(Role.MEMBER));

        userInfoDto = new UserInfoDto(1L, "testuser", Set.of(Role.MEMBER), 
                List.of(new SimpleGrantedAuthority("ROLE_MEMBER")));
    }

    @Test
    void register_shouldReturnSuccessMessage() {
        // given
        doNothing().when(authService).registerUser(any(RegisterRequest.class));

        // when
        Map<String, String> response = authController.register(registerRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.get("message")).isEqualTo("User registered successfully!");
        assertThat(response.get("username")).isEqualTo("testuser");
        verify(authService, times(1)).registerUser(registerRequest);
    }

    @Test
    void login_shouldReturnOAuth2Endpoint() {
        // when
        ResponseEntity<?> response = authController.login(loginRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.get("message")).isEqualTo("Please use OAuth2 authorization endpoint");
        assertThat(responseBody.get("authorization_endpoint")).isEqualTo("/oauth2/authorize");
    }

    @Test
    void getUserInfo_shouldReturnUserInfo() {
        // given
        when(authService.getUserInfo(authentication)).thenReturn(userInfoDto);

        // when
        UserInfoDto result = authController.getUserInfo(authentication);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getRoles()).contains(Role.MEMBER);
        verify(authService, times(1)).getUserInfo(authentication);
    }

    @Test
    void getUserIdsByRole_shouldReturnUserIds() {
        // given
        List<Long> userIds = List.of(1L, 2L, 3L);
        when(authService.getUserIdsByRole("MEMBER", 0, 20)).thenReturn(userIds);

        // when
        List<Long> result = authController.getUserIdsByRole("MEMBER", 0, 20);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result).containsExactly(1L, 2L, 3L);
        verify(authService, times(1)).getUserIdsByRole("MEMBER", 0, 20);
    }

    @Test
    void getUserRolesById_shouldReturnUserRoles() {
        // given
        Set<Role> roles = Set.of(Role.MEMBER, Role.COACH);
        when(authService.getUserRolesById(1L)).thenReturn(roles);

        // when
        Set<Role> result = authController.getUserRolesById(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).contains(Role.MEMBER, Role.COACH);
        verify(authService, times(1)).getUserRolesById(1L);
    }
}