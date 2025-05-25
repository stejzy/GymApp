package org.zzpj.gymapp.scheduleservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zzpj.gymapp.scheduleservice.model.RecurringGroupClassSchedule;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface RecurringGroupClassScheduleRepository extends JpaRepository<RecurringGroupClassSchedule, Long> {

    @Query("""
        SELECT r FROM RecurringGroupClassSchedule r
        WHERE r.gymGroupClassOffering.gym.id = :gymId
          AND r.dayOfWeek = :dayOfWeek
          AND r.startDate <= :targetDate AND r.endDate >= :targetDate
          AND (
            (r.startTime <= :startTime AND r.endTime > :startTime)
            OR
            (r.startTime < :endTime AND r.endTime >= :endTime)
            OR
            (r.startTime >= :startTime AND r.endTime <= :endTime)
          )
    """)
    List<RecurringGroupClassSchedule> findConflictingSchedulesOnDate(
        @Param("gymId") Long gymId,
        @Param("dayOfWeek") DayOfWeek dayOfWeek,
        @Param("targetDate") LocalDate targetDate,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime
    );

    List<RecurringGroupClassSchedule> findAllByGymGroupClassOffering_Gym_Id(Long gymId);

    @Query("SELECT r FROM RecurringGroupClassSchedule r WHERE r.endDate >= :start AND r.startDate <= :end")
    List<RecurringGroupClassSchedule> findActiveSchedules(@Param("start") LocalDate start, @Param("end") LocalDate end);


}
