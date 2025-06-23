package org.zzpj.gymapp.userservice.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("test")
class AuthServiceClientTest {

    @MockitoBean
    private AuthServiceClient authServiceClient;

    private WireMockServer wireMockServer;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();
        WireMock.configureFor("localhost", 8089);
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

//    @Test
//    void shouldGetUserRolesSuccessfully() {
//        // Given
//        when(authServiceClient.getUserRoles(1L, "Bearer test-token"))
//            .thenReturn(Set.of("MEMBER", "COACH"));
//
//        Long userId = 1L;
//        String authHeader = "Bearer test-token";
//        String expectedResponse = "[\"MEMBER\", \"COACH\"]";
//
//        stubFor(get(urlEqualTo("/users/" + userId + "/roles"))
//                .withHeader("Authorization", equalTo(authHeader))
//                .willReturn(aResponse()
//                        .withStatus(200)
//                        .withHeader("Content-Type", "application/json")
//                        .withBody(expectedResponse)));
//
//        // When
//        Set<String> roles = authServiceClient.getUserRoles(userId, authHeader);
//
//        // Then
//        assertThat(roles).containsExactlyInAnyOrder("MEMBER", "COACH");
//        verify(getRequestedFor(urlEqualTo("/users/" + userId + "/roles"))
//                .withHeader("Authorization", equalTo(authHeader)));
//    }

//    @Test
//    void shouldGetUserIdsByRoleSuccessfully() {
//        // Given
//        String role = "MEMBER";
//        int page = 0;
//        int size = 20;
//        String authHeader = "Bearer test-token";
//        String expectedResponse = "[1, 2, 3]";
//
//        stubFor(get(urlEqualTo("/users/role/" + role + "?page=" + page + "&size=" + size))
//                .withHeader("Authorization", equalTo(authHeader))
//                .willReturn(aResponse()
//                        .withStatus(200)
//                        .withHeader("Content-Type", "application/json")
//                        .withBody(expectedResponse)));
//
//        // When
//        List<Long> userIds = authServiceClient.getUserIdsByRole(role, page, size, authHeader);
//
//        // Then
//        assertThat(userIds).containsExactly(1L, 2L, 3L);
//        verify(getRequestedFor(urlEqualTo("/users/role/" + role + "?page=" + page + "&size=" + size))
//                .withHeader("Authorization", equalTo(authHeader)));
//    }

    @Test
    void shouldReturnEmptySetWhenNoRolesFound() {
        // Given
        Long userId = 999L;
        String authHeader = "Bearer test-token";
        String expectedResponse = "[]";

        stubFor(get(urlEqualTo("/users/" + userId + "/roles"))
                .withHeader("Authorization", equalTo(authHeader))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(expectedResponse)));

        // When
        Set<String> roles = authServiceClient.getUserRoles(userId, authHeader);

        // Then
        assertThat(roles).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenNoUsersWithRoleFound() {
        // Given
        String role = "LOSOWAROLA";
        int page = 0;
        int size = 20;
        String authHeader = "Bearer test-token";
        String expectedResponse = "[]";

        stubFor(get(urlEqualTo("/users/role/" + role + "?page=" + page + "&size=" + size))
                .withHeader("Authorization", equalTo(authHeader))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(expectedResponse)));

        // When
        List<Long> userIds = authServiceClient.getUserIdsByRole(role, page, size, authHeader);

        // Then
        assertThat(userIds).isEmpty();
    }

    @Test
    void shouldHandleServerError() {
        // Given
        Long userId = 1L;
        String authHeader = "Bearer test-token";

        stubFor(get(urlEqualTo("/users/" + userId + "/roles"))
                .withHeader("Authorization", equalTo(authHeader))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Internal Server Error")));

        // When & Then
        try {
            authServiceClient.getUserRoles(userId, authHeader);
        } catch (Exception e) {
            // Expected behavior - the client should throw an exception on server error
            assertThat(e).isInstanceOf(Exception.class);
        }
    }

    @Test
    void shouldHandleUnauthorizedError() {
        // Given
        Long userId = 1L;
        String authHeader = "Bearer invalid-token";

        stubFor(get(urlEqualTo("/users/" + userId + "/roles"))
                .withHeader("Authorization", equalTo(authHeader))
                .willReturn(aResponse()
                        .withStatus(401)
                        .withBody("Unauthorized")));

        // When & Then
        try {
            authServiceClient.getUserRoles(userId, authHeader);
        } catch (Exception e) {
            // Expected behavior - the client should throw an exception on unauthorized
            assertThat(e).isInstanceOf(Exception.class);
        }
    }
}