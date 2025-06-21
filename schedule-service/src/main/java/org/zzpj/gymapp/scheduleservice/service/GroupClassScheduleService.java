package org.zzpj.gymapp.scheduleservice.service;

import jakarta.ws.rs.NotFoundException;
import org.springframework.stereotype.Service;
import org.zzpj.gymapp.scheduleservice.dto.GroupClassScheduleDTO;
import org.zzpj.gymapp.scheduleservice.model.GroupClassSchedule;
import org.zzpj.gymapp.scheduleservice.model.SessionType;
import org.zzpj.gymapp.scheduleservice.model.TrainingSession;
import org.zzpj.gymapp.scheduleservice.repository.GroupClassScheduleRepository;
import org.zzpj.gymapp.scheduleservice.repository.TrainingSessionRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupClassScheduleService {

    private final GroupClassScheduleRepository groupClassScheduleRepository;
    private final TrainingSessionRepository trainingSessionRepository;

    public GroupClassScheduleService(GroupClassScheduleRepository groupClassScheduleRepository,
                                     TrainingSessionRepository trainingSessionRepository) {
        this.groupClassScheduleRepository = groupClassScheduleRepository;
        this.trainingSessionRepository = trainingSessionRepository;
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

//        // ✅ Sprawdzenie dostępności trenera
//        List<TrainingSession> trainerConflicts = trainingSessionRepository.findTrainerConflicts(
//                schedule.getTrainerId(), schedule.getStartTime(), schedule.getEndTime());
//
//        if (!trainerConflicts.isEmpty()) {
//            throw new IllegalArgumentException("Trener ma już zaplanowaną inną sesję w tym czasie.");
//        }

        // Zapis harmonogramu
        GroupClassSchedule savedSchedule = groupClassScheduleRepository.save(schedule);
//
//        // 📝 Zapis sesji trenera
//        TrainingSession trainerSession = new TrainingSession();
//        trainerSession.setTrainerId(schedule.getTrainerId());
//        trainerSession.setUserId(null); // tylko trener
//        trainerSession.setStartTime(schedule.getStartTime());
//        trainerSession.setEndTime(schedule.getEndTime());
//        trainerSession.setClassId(savedSchedule.getId());
//        trainerSession.setType(SessionType.GROUP);
//
//        trainingSessionRepository.save(trainerSession);

        return savedSchedule;
    }

    public GroupClassSchedule signUpUser(Long scheduleId, Long userId) {
        GroupClassSchedule schedule = groupClassScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new NotFoundException("Schedule not found"));

        if (schedule.getParticipantIds().contains(userId)) {
            throw new IllegalStateException("User already signed up");
        }

        if (schedule.getParticipantIds().size() >= schedule.getCapacity()) {
            throw new IllegalStateException("Schedule is full");
        }

        // TODO: Check for schedule conflicts with user's personal/group classes
        // For example: userScheduleRepository.findByUserIdAndOverlapping(...)

        schedule.getParticipantIds().add(userId);
        return groupClassScheduleRepository.save(schedule);
    }

    public List<GroupClassScheduleDTO> getAllGroupClassScheduleDTOs() {
        return groupClassScheduleRepository.findAll().stream()
                .map(schedule -> {
                    GroupClassScheduleDTO dto = new GroupClassScheduleDTO();
                    dto.setId(schedule.getId());
                    dto.setGymGroupClassOfferingName(schedule.getGymGroupClassOffering().getGroupClassDefinition().getName());
                    dto.setTrainerId(schedule.getTrainerId());
                    dto.setStartTime(schedule.getStartTime());
                    dto.setEndTime(schedule.getEndTime());
                    dto.setParticipantIds(schedule.getParticipantIds());
                    dto.setCapacity(schedule.getCapacity());
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
