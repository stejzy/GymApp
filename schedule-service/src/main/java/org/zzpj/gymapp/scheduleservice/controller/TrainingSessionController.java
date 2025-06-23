package org.zzpj.gymapp.scheduleservice.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zzpj.gymapp.scheduleservice.exeption.AccessDeniedException;
import org.zzpj.gymapp.scheduleservice.model.SessionStatus;
import org.zzpj.gymapp.scheduleservice.model.TrainingSession;
import org.zzpj.gymapp.scheduleservice.service.TrainingSessionService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/training-sessions")
public class TrainingSessionController {

    private final TrainingSessionService trainingSessionService;

    public TrainingSessionController(TrainingSessionService trainingSessionService) {
        this.trainingSessionService = trainingSessionService;
    }

    @PostMapping("/create-individual")
    public ResponseEntity<TrainingSession> createIndividualTrainingSession(
            @RequestParam("trainerId") Long trainerId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam("startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam("endTime") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Roles") String rolesHeader) {

        TrainingSession session = trainingSessionService.createIndividualTrainingSession(
                trainerId,
                date,
                startTime,
                endTime,
                userId,
                rolesHeader
        );
        return ResponseEntity.ok(session);
    }


    @GetMapping("/staging")
    public ResponseEntity<List<TrainingSession>> getStagingSessions(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Roles") String rolesHeader) {

        if (rolesHeader.contains("COACH")) {
            List<TrainingSession> sessions = trainingSessionService.getStagingSessionsForTrainer(userId);
            return ResponseEntity.ok(sessions);
        } else if (rolesHeader.contains("MEMBER")) {
            List<TrainingSession> sessions = trainingSessionService.getStagingSessionsForUser(userId);
            return ResponseEntity.ok(sessions);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<TrainingSession>> getAllSessions(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Roles") String rolesHeader) {

        if (rolesHeader.contains("COACH")) {
            List<TrainingSession> sessions = trainingSessionService.getAllSessionsForTrainer(userId);
            return ResponseEntity.ok(sessions);
        } else if (rolesHeader.contains("MEMBER")) {
            List<TrainingSession> sessions = trainingSessionService.getAllSessionsForUser(userId);
            return ResponseEntity.ok(sessions);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/respond")
    public ResponseEntity<TrainingSession> respondToSession(
            @RequestParam Long sessionId,
            @RequestParam boolean accept,
            @RequestHeader("X-User-Id") Long trainerId,
            @RequestHeader("X-User-Roles") String rolesHeader) {

        if (!rolesHeader.contains("COACH")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        TrainingSession updated = trainingSessionService.respondToIndividualSession(sessionId, trainerId, accept);
        return ResponseEntity.ok(updated);
    }



}
