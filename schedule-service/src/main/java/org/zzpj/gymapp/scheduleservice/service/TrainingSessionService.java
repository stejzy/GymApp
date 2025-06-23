package org.zzpj.gymapp.scheduleservice.service;


import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.zzpj.gymapp.scheduleservice.exeption.AccessDeniedException;
import org.zzpj.gymapp.scheduleservice.exeption.ScheduleConflictException;
import org.zzpj.gymapp.scheduleservice.model.SessionStatus;
import org.zzpj.gymapp.scheduleservice.model.SessionType;
import org.zzpj.gymapp.scheduleservice.model.TrainingSession;
import org.zzpj.gymapp.scheduleservice.repository.TrainingSessionRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class TrainingSessionService {

    private final TrainingSessionRepository trainingSessionRepository;

    public TrainingSessionService(TrainingSessionRepository trainingSessionRepository) {
        this.trainingSessionRepository = trainingSessionRepository;
    }

    public void checkTrainerAvailability(Long trainerId, LocalDateTime start, LocalDateTime end) {
        List<TrainingSession> conflicts = trainingSessionRepository.findTrainerConflicts(trainerId, start, end)
                .stream()
                .filter(s -> s.getStatus() == SessionStatus.CONFIRMED)
                .toList();
        if (!conflicts.isEmpty()) {
            throw new ScheduleConflictException("Trainer has another session at this time.");
        }
    }

    public void checkUserAvailability(Long userId, LocalDateTime start, LocalDateTime end) {
        List<TrainingSession> conflicts = trainingSessionRepository.findUserConflicts(userId, start, end)
                .stream()
                .filter(s -> s.getStatus() == SessionStatus.CONFIRMED)
                .toList();
        if (!conflicts.isEmpty()) {
            throw new ScheduleConflictException("User has another session at this time.");
        }
    }

    @Transactional
    public TrainingSession createGroupTrainingSession(TrainingSession session) {
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

    @Transactional
    public TrainingSession createIndividualTrainingSession(Long trainerId, LocalDate date,
                                                           LocalTime startTime, LocalTime endTime,
                                                           Long userId, String rolesHeader) {
        boolean isCoach = rolesHeader != null && rolesHeader.contains("COACH");

        if (isCoach) {
            throw new AccessDeniedException("Only users (non-coaches) can create individual training sessions.");
        }

        LocalDateTime start = LocalDateTime.of(date, startTime);
        LocalDateTime end = LocalDateTime.of(date, endTime);

        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("Start time must be before end time.");
        }

        // Walidacje dostępności
        checkTrainerAvailability(trainerId, start, end);
        checkUserAvailability(userId, start, end);

        // Tworzenie sesji
        TrainingSession session = new TrainingSession();
        session.setUserId(userId);
        session.setTrainerId(trainerId);
        session.setType(SessionType.INDIVIDUAL_COACH);
        session.setStatus(SessionStatus.STAGING);
        session.setStartTime(start);
        session.setEndTime(end);

        return trainingSessionRepository.save(session);
    }

    @Transactional
    public TrainingSession respondToIndividualSession(Long sessionId, Long trainerId, boolean accept) {
        TrainingSession session = trainingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Training session not found."));

        if (!session.getTrainerId().equals(trainerId)) {
            throw new AccessDeniedException("You are not assigned to this session.");
        }

        if (session.getType() != SessionType.INDIVIDUAL_COACH) {
            throw new IllegalStateException("Only individual sessions can be accepted or rejected.");
        }

        if (session.getStatus() != SessionStatus.STAGING) {
            throw new IllegalStateException("Only staging sessions can be accepted or rejected.");
        }

        if (accept) {
           try {
                checkTrainerAvailability(session.getTrainerId(), session.getStartTime(), session.getEndTime());
            } catch (ScheduleConflictException e) {
                throw new ScheduleConflictException("You have another session at this time.");
            }
            checkUserAvailability(session.getUserId(), session.getStartTime(), session.getEndTime());
            session.setStatus(SessionStatus.CONFIRMED);
        } else {
            session.setStatus(SessionStatus.REJECTED);
        }

        return trainingSessionRepository.save(session);
    }

    public List<TrainingSession> getStagingSessionsForUser(Long userId) {
        if (userId == null) {
            return List.of();
        }
        return trainingSessionRepository.findAll()
                .stream()
                .filter(s -> s.getUserId() != null && s.getUserId().equals(userId) && s.getStatus() == SessionStatus.STAGING)
                .toList();
    }

    public List<TrainingSession> getStagingSessionsForTrainer(Long trainerId) {
        if (trainerId == null) {
            return List.of(); // Zwraca pustą listę, jeśli trainerId jest null
        }
        return trainingSessionRepository.findAll()
                .stream()
                .filter(s -> s.getTrainerId() != null && s.getTrainerId().equals(trainerId) && s.getStatus() == SessionStatus.STAGING)
                .toList();
    }



   public List<TrainingSession> getAllSessionsForUser(Long userId) {
       if (userId == null) {
           return List.of();
       }
       return trainingSessionRepository.findAll()
               .stream()
               .filter(s -> s.getUserId() != null && s.getUserId().equals(userId))
               .toList();
   }

   public List<TrainingSession> getAllSessionsForTrainer(Long trainerId) {
       if (trainerId == null) {
           return List.of();
       }
       return trainingSessionRepository.findAll()
               .stream()
               .filter(s -> s.getTrainerId() != null && s.getTrainerId().equals(trainerId))
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
