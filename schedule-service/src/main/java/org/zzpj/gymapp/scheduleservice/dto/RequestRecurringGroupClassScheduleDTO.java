package org.zzpj.gymapp.scheduleservice.dto;

import org.zzpj.gymapp.scheduleservice.model.Frequency;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

public record RequestRecurringGroupClassScheduleDTO(
     Long gymGroupClassOfferingId,
     Long trainerId,
     DayOfWeek dayOfWeek,
     LocalTime startTime,
     LocalTime endTime,
     LocalDate startDate,
     LocalDate endDate,
     Frequency frequency,
     Integer capacity
){}
