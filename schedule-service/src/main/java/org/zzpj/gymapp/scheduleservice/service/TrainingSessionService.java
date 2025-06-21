package org.zzpj.gymapp.scheduleservice.service;


import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.zzpj.gymapp.scheduleservice.exeption.ScheduleConflictException;
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

    @Transactional
    public TrainingSession createTrainingSession(TrainingSession session) {
        LocalDateTime start = session.getStartTime();
        LocalDateTime end = session.getEndTime();

        // Konflikt z trenerem
        List<TrainingSession> trainerConflicts = trainingSessionRepository.findTrainerConflicts(
                session.getTrainerId(), start, end);
        if (!trainerConflicts.isEmpty()) {
            throw new ScheduleConflictException("Trainer has another session at this time.");
        }

        // Konflikt z użytkownikiem
        List<TrainingSession> userConflicts = trainingSessionRepository.findUserConflicts(
                session.getUserId(), start, end);
        if (!userConflicts.isEmpty()) {
            throw new ScheduleConflictException("User has another session at this time.");
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
