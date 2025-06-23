package org.zzpj.gymapp.authservice.exception;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.zzpj.gymapp.authservice.controller.TestExceptionController;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TestExceptionController.class,
        excludeAutoConfiguration = {
                org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration.class
        }
)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldHandleUserNotFoundException() throws Exception {
        mockMvc.perform(get("/test/user-not-found")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("User not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.instance").exists());
    }

    @Test
    void shouldHandleUnauthorizedException() throws Exception {
        mockMvc.perform(get("/test/unauthorized")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Unauthorized"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.instance").exists());
    }

    @Test
    void shouldHandleUserAlreadyExistsException() throws Exception {
        mockMvc.perform(get("/test/user-already-exists")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("User already exists"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.instance").exists());
    }

    @Test
    void shouldHandleIllegalArgumentException() throws Exception {
        mockMvc.perform(get("/test/illegal-argument")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.instance").exists());
    }

    @Test
    void shouldHandleGenericException() throws Exception {
        mockMvc.perform(get("/test/generic-exception")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("Internal server error"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.instance").exists());
    }
} 