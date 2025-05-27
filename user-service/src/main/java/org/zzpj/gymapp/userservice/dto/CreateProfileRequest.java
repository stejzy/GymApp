package org.zzpj.gymapp.userservice.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CreateProfileRequest extends BaseProfileRequest {
    @NotNull
    private Long userId;
}