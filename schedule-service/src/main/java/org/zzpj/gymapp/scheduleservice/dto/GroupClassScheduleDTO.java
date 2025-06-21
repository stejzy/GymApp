package org.zzpj.gymapp.scheduleservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
public class GroupClassScheduleDTO {
    // Gettery i settery
    private Long id;
    private String gymGroupClassOfferingName; // Nazwa oferty zajęć grupowych
    private Long trainerId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<Long> participantIds; // Lista ID uczestników
    private Integer capacity;

}