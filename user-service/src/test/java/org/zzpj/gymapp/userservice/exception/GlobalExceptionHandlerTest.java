package org.zzpj.gymapp.userservice.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/test");
    }

    @Test
    void shouldHandleUserProfileNotFound() {
        // Given
        UserProfileNotFoundException exception = new UserProfileNotFoundException("Profile not found");

        // When
        ProblemDetail result = exceptionHandler.handleUserProfileNotFound(exception, request);

        // Then
        assertThat(result.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(result.getTitle()).isEqualTo("User profile not found");
        assertThat(result.getDetail()).isEqualTo("Profile not found");
        assertThat(result.getInstance()).isNotNull();
    }

    @Test
    void shouldHandleUserProfileAlreadyExists() {
        // Given
        UserProfileAlreadyExistsException exception = new UserProfileAlreadyExistsException("Profile already exists");

        // When
        ProblemDetail result = exceptionHandler.handleUserProfileAlreadyExists(exception, request);

        // Then
        assertThat(result.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(result.getTitle()).isEqualTo("User profile already exists");
        assertThat(result.getDetail()).isEqualTo("Profile already exists");
        assertThat(result.getInstance()).isNotNull();
    }

    @Test
    void shouldHandleUnauthorized() {
        // Given
        UnauthorizedException exception = new UnauthorizedException("Unauthorized access");

        // When
        ProblemDetail result = exceptionHandler.handleUnauthorized(exception, request);

        // Then
        assertThat(result.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(result.getTitle()).isEqualTo("Unauthorized");
        assertThat(result.getDetail()).isEqualTo("Unauthorized access");
        assertThat(result.getInstance()).isNotNull();
    }

    @Test
    void shouldHandleBadRequest() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");

        // When
        ProblemDetail result = exceptionHandler.handleBadRequest(exception, request);

        // Then
        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(result.getTitle()).isEqualTo("Bad request");
        assertThat(result.getDetail()).isEqualTo("Invalid argument");
        assertThat(result.getInstance()).isNotNull();
    }

    @Test
    void shouldHandleValidationException() {
        // Given
        FieldError fieldError = new FieldError("object", "field", "default message");
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
                null, new org.springframework.validation.BeanPropertyBindingResult(new Object(), "object")
        );
        exception.getBindingResult().addError(fieldError);

        // When
        ProblemDetail result = exceptionHandler.handleValidationException(exception, request);

        // Then
        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(result.getTitle()).isEqualTo("Validation failed");
        assertThat(result.getDetail()).contains("field: default message");
        assertThat(result.getInstance()).isNotNull();
    }

    @Test
    void shouldHandleGenericException() {
        // Given
        RuntimeException exception = new RuntimeException("Something went wrong");

        // When
        ProblemDetail result = exceptionHandler.handleOther(exception, request);

        // Then
        assertThat(result.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(result.getTitle()).isEqualTo("Internal server error");
        assertThat(result.getDetail()).isEqualTo("Something went wrong");
        assertThat(result.getInstance()).isNotNull();
    }
} 