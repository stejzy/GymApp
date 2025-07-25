package org.zzpj.gymapp.scheduleservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

@Setter
@Getter
@AllArgsConstructor
public class UserProfileResponse {
    private Long id;
    private Long userId;
    private String firstName;
    private String lastName;
    private String gender;
    private Double height;
    private Double weight;
    private LocalDate birthday;
    private String phone;
    private TrainingLevel level;
    private String bio;
    private String avatarUrl;
    private Set<String> roles;
}

