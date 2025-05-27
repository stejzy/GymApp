package org.zzpj.gymapp.scheduleservice.exeption;

public record ErrorResponse(String message, int statusCode, String timestamp) {}