package org.zzpj.gymapp.scheduleservice.service;


import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.zzpj.gymapp.scheduleservice.exeption.ScheduleConflictException;
import org.zzpj.gymapp.scheduleservice.model.SessionType;
import org.zzpj.gymapp.scheduleservice.model.TrainingSession;
import org.zzpj.gymapp.scheduleservice.repository.TrainingSessionRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TrainingSessionService {

    private final TrainingSessionRepository trainingSessionRepository;

    public TrainingSessionService(TrainingSessionRepository trainingSessionRepository) {
        this.trainingSessionRepository = trainingSessionRepository;
    }

    public void checkTrainerAvailability(Long trainerId, LocalDateTime start, LocalDateTime end) {
        List<TrainingSession> conflicts = trainingSessionRepository.findTrainerConflicts(trainerId, start, end);
        if (!conflicts.isEmpty()) {
            throw new ScheduleConflictException("Trainer has another session at this time.");
        }
    }

    public void checkUserAvailability(Long userId, LocalDateTime start, LocalDateTime end) {
        List<TrainingSession> conflicts = trainingSessionRepository.findUserConflicts(userId, start, end);
        if (!conflicts.isEmpty()) {
            throw new ScheduleConflictException("User has another session at this time.");
        }
    }

    @Transactional
    public TrainingSession createTrainingSession(TrainingSession session) {
        LocalDateTime start = session.getStartTime();
        LocalDateTime end = session.getEndTime();

        if (start == null || end == null || session.getTrainerId() == null) {
            throw new IllegalArgumentException("All session fields must be provided.");
        }
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("Start time must be before end time.");
        }

        // Sprawdzenie konfliktów trenera, ale ignoruj konflikt jeśli to ta sama sesja grupowa
        List<TrainingSession> trainerConflicts = trainingSessionRepository.findTrainerConflicts(session.getTrainerId(), start, end);
        for (TrainingSession conflict : trainerConflicts) {
            if (!(conflict.getClassId().equals(session.getClassId()) && conflict.getType() == SessionType.GROUP)) {
                throw new ScheduleConflictException("Trainer has another session at this time.");
            }
        }

        if (session.getUserId() != null) {
            // Sprawdzenie konfliktów użytkownika, ale ignoruj konflikt jeśli to ta sama sesja grupowa
            List<TrainingSession> userConflicts = trainingSessionRepository.findUserConflicts(session.getUserId(), start, end);
            for (TrainingSession conflict : userConflicts) {
                if (!(conflict.getClassId().equals(session.getClassId()) && conflict.getType() == SessionType.GROUP)) {
                    throw new ScheduleConflictException("User has another session at this time.");
                }
            }
        }

        return trainingSessionRepository.save(session);
    }


    public List<TrainingSession> getAllSessionsForUser(Long userId) {
        return trainingSessionRepository.findAll()
                .stream()
                .filter(s -> s.getUserId().equals(userId))
                .toList();
    }

    public List<TrainingSession> getAllSessionsForTrainer(Long trainerId) {
        return trainingSessionRepository.findAll()
                .stream()
                .filter(s -> s.getTrainerId().equals(trainerId))
                .toList();
    }

    public TrainingSession getById(Long id) {
        return trainingSessionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Training session not found."));
    }

    public void deleteSession(Long id) {
        trainingSessionRepository.deleteById(id);
    }
}
