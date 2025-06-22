package org.zzpj.gymapp.scheduleservice.exeption;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TestExceptionController.class,
        excludeAutoConfiguration = {
                org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration.class
        }
)
@Import(GlobalExceptionHandler.class)

class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldHandleEntityNotFoundException() throws Exception {
        mockMvc.perform(get("/test/not-found")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Entity not found"))
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldHandleScheduleConflictException() throws Exception {
        mockMvc.perform(get("/test/conflict")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Schedule conflict occurred"))
                .andExpect(jsonPath("$.statusCode").value(409))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
