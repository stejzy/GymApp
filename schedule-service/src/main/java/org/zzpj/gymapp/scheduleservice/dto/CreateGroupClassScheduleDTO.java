package org.zzpj.gymapp.scheduleservice.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.StringJoiner;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateGroupClassScheduleDTO {
    private Long gymGroupClassOfferingId;
    private Long trainerId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer capacity;

    @Override
    public String toString() {
        return new StringJoiner(", ", CreateGroupClassScheduleDTO.class.getSimpleName() + "[", "]")
                .add("gymGroupClassOfferingId=" + gymGroupClassOfferingId)
                .add("trainerId=" + trainerId)
                .add("startTime=" + startTime)
                .add("endTime=" + endTime)
                .add("capacity=" + capacity)
                .toString();
    }
}
