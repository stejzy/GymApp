package org.zzpj.gymapp.scheduleservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GroupClassScheduleDTO {
    // Gettery i settery
    private Long id;
    private String gymGroupClassOfferingName;
    private Long trainerId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<Long> participantIds;
    private Integer capacity;

}