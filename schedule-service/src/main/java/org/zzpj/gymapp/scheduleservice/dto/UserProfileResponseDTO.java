package org.zzpj.gymapp.scheduleservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponseDTO {
    private Long id;
    private Long userId;
    private String firstName;
    private String lastName;
    private String gender;
    private Double height;
    private Double weight;
    private LocalDate birthday;
    private String phone;
    private String level;
    private String bio;
    private String avatarUrl;
    private Set<String> roles;
}

