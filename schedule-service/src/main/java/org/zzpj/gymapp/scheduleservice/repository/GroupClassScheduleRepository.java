package org.zzpj.gymapp.scheduleservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zzpj.gymapp.scheduleservice.model.GroupClassSchedule;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GroupClassScheduleRepository extends JpaRepository<GroupClassSchedule, Long> {
    @Query("""
        SELECT g FROM GroupClassSchedule g
        WHERE g.gymGroupClassOffering.gym.id = :gymId
          AND (
            (g.startTime <= :start AND g.endTime > :start)
            OR
            (g.startTime < :end AND g.endTime >= :end)
            OR
            (g.startTime >= :start AND g.endTime <= :end)
          )
    """)
    List<GroupClassSchedule> findConflictingGroupSchedules(
            @Param("gymId") Long gymId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}