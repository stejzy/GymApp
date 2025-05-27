package org.zzpj.gymapp.userservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public abstract class BaseProfileRequest {
    @Size(min = 2, max = 50, message = "First name must not exceed 50 characters.")
    private String firstName;

    @Size(min = 2, max = 50, message = "Last name must not exceed 50 characters.")
    private String lastName;

    @Size(min = 1, max = 10, message = "Gender must not exceed 10 characters.")
    private String gender;

    @DecimalMin(value = "0.0", inclusive = false, message = "Height must be greater than 0.")
    @DecimalMax(value = "300.0", message = "Height must not exceed 300 cm.")
    private Double height;

    @DecimalMin(value = "0.0", inclusive = false, message = "Weight must be greater than 0.")
    @DecimalMax(value = "500.0", message = "Weight must not exceed 500 kg.")
    private Double weight;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Past(message = "Birthday must be in the past.")
    private LocalDate birthday;

    @Pattern(
            regexp = "^\\+?\\d{9,15}$",
            message = "Phone has to be a valid phone number(9-15 digits, optionally with a plus at the beginning)"
    )
    private String phone;

    private TrainingLevel level;

    @Size(max = 500, message = "Bio must not exceed 500 characters.")
    private String bio;

    @Size(max = 255, message = "Avatar URL must not exceed 255 characters.")
    @Pattern(
            regexp = "^(https?://).*$",
            message = "Avatar URL must be a valid URL starting with http:// or https://"
    )
    private String avatarUrl;
}
