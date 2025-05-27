package org.zzpj.gymapp.scheduleservice.service;

import org.springframework.stereotype.Service;
import org.zzpj.gymapp.scheduleservice.model.GroupClassSchedule;
import org.zzpj.gymapp.scheduleservice.repository.GroupClassScheduleRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GroupClassScheduleService {

    private final GroupClassScheduleRepository groupClassScheduleRepository;

    public GroupClassScheduleService(GroupClassScheduleRepository groupClassScheduleRepository) {
        this.groupClassScheduleRepository = groupClassScheduleRepository;
    }

    public GroupClassSchedule addGroupClassSchedule(GroupClassSchedule schedule) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneMonthAhead = now.plusMonths(1);

        if (schedule.getStartTime().isAfter(oneMonthAhead)) {
            throw new IllegalArgumentException("Zajęcia mogą być tworzone maksymalnie miesiąc do przodu.");
        }

        Long gymId = schedule.getGymGroupClassOffering().getGym().getId();
        List<GroupClassSchedule> conflicts = groupClassScheduleRepository.findConflictingGroupSchedules(
                gymId, schedule.getStartTime(), schedule.getEndTime());

        if (!conflicts.isEmpty()) {
            throw new IllegalArgumentException("Kolizja z istniejącymi zajęciami.");
        }

        return groupClassScheduleRepository.save(schedule);
    }
}
