package org.zzpj.gymapp.scheduleservice.dto;

import java.time.LocalTime;

public record GymDTO(
        Long id,
        String name,
        String city,
        String address,
        String phoneNumber,
        LocalTime openingHour,
        LocalTime closingHour
) {}
