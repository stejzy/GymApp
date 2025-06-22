package org.zzpj.gymapp.scheduleservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainerSummaryDTO {
    private Long id;
    private Long userId;
    private String firstName;
    private String lastName;
    private String phone;
    private String bio;
    private List<String> roles;
}
