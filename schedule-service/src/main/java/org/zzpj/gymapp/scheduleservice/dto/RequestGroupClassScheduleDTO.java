package org.zzpj.gymapp.scheduleservice.dto;

import java.time.LocalDateTime;

public record RequestGroupClassScheduleDTO (

     Long gymGroupClassOfferingId,

     Long trainerId,

     LocalDateTime startTime,

     LocalDateTime endTime,

     Integer capacity
){}
