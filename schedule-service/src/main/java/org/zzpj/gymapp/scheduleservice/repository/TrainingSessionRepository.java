package org.zzpj.gymapp.scheduleservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.zzpj.gymapp.scheduleservice.model.TrainingSession;

import java.time.LocalDateTime;
import java.util.List;

public interface TrainingSessionRepository extends JpaRepository<TrainingSession, Long> {
    @Query("""
    SELECT t FROM TrainingSession t
    WHERE t.trainerId = :trainerId
      AND t.startTime < :endTime
      AND t.endTime > :startTime
    """)
    List<TrainingSession> findTrainerConflicts(Long trainerId, LocalDateTime startTime, LocalDateTime endTime);

    @Query("""
    SELECT t FROM TrainingSession t
    WHERE t.userId = :userId
      AND t.startTime < :endTime
      AND t.endTime > :startTime
    """)
    List<TrainingSession> findUserConflicts(Long userId, LocalDateTime startTime, LocalDateTime endTime);


}
