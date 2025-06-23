package org.zzpj.gymapp.scheduleservice.service;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.springframework.stereotype.Service;
import org.zzpj.gymapp.scheduleservice.client.UserServiceClient;
import org.zzpj.gymapp.scheduleservice.dto.CreateGroupClassScheduleDTO;
import org.zzpj.gymapp.scheduleservice.dto.GroupClassScheduleDTO;
import org.zzpj.gymapp.scheduleservice.dto.UserProfileResponseDTO;
import org.zzpj.gymapp.scheduleservice.exeption.ScheduleConflictException;
import org.zzpj.gymapp.scheduleservice.exeption.TrainerBusyException;
import org.zzpj.gymapp.scheduleservice.exeption.TrainerNotAssignedToGymException;
import org.zzpj.gymapp.scheduleservice.model.*;
import org.zzpj.gymapp.scheduleservice.repository.GroupClassScheduleRepository;
import org.zzpj.gymapp.scheduleservice.repository.GymGroupClassOfferingRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupClassScheduleService {

    private final GroupClassScheduleRepository groupClassScheduleRepository;
    private final TrainingSessionService trainingSessionService;
    private final GymGroupClassOfferingRepository gymGroupClassOfferingRepository;
    private final UserServiceClient userServiceClient;

    public GroupClassScheduleService(GroupClassScheduleRepository groupClassScheduleRepository,
                                     TrainingSessionService trainingSessionService,
                                     GymGroupClassOfferingRepository gymGroupClassOfferingRepository,
                                     UserServiceClient userServiceClient) {
        this.groupClassScheduleRepository = groupClassScheduleRepository;
        this.trainingSessionService = trainingSessionService;
        this.gymGroupClassOfferingRepository = gymGroupClassOfferingRepository;
        this.userServiceClient = userServiceClient;
    }

    @Transactional
    public GroupClassScheduleDTO  addGroupClassSchedule(CreateGroupClassScheduleDTO dto) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);
        LocalDateTime oneMonthAhead = now.plusMonths(1);

        if (dto.getStartTime().isBefore(tomorrow)) {
            throw new IllegalArgumentException("Zajęcia muszą być tworzone z jednodniowym wyprzedzeniem.");
        }

        if (dto.getStartTime().isAfter(oneMonthAhead)) {
            throw new IllegalArgumentException("Zajęcia mogą być tworzone maksymalnie miesiąc do przodu.");
        }

        GymGroupClassOffering gymGroupClassOffering = gymGroupClassOfferingRepository
                .findById(dto.getGymGroupClassOfferingId())
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono oferty grupowych zajęć."));

        Gym gym = gymGroupClassOffering.getGym();
        Long trainerId = dto.getTrainerId();

        if (gym.getTrainerIds() == null || !gym.getTrainerIds().contains(trainerId)) {
            throw new TrainerNotAssignedToGymException("Trener z ID " + trainerId + " nie jest przypisany do siłowni oferującej te zajęcia.");
        }

        Long gymId = gymGroupClassOffering.getGym().getId();

        List<GroupClassSchedule> conflicts = groupClassScheduleRepository.findConflictingGroupSchedules(
                gymId, dto.getStartTime(), dto.getEndTime());

        if (!conflicts.isEmpty()) {
            throw new IllegalArgumentException("Kolizja z istniejącymi zajęciami.");
        }

        try {
            trainingSessionService.checkTrainerAvailability(
                    dto.getTrainerId(), dto.getStartTime(), dto.getEndTime());
        } catch (ScheduleConflictException e) {
            throw new TrainerBusyException("Trener jest zajęty w terminie: " +
                    dto.getStartTime() + " - " + dto.getEndTime());
        }

        GroupClassSchedule schedule = new GroupClassSchedule();
        schedule.setGymGroupClassOffering(gymGroupClassOffering);
        schedule.setTrainerId(dto.getTrainerId());
        schedule.setStartTime(dto.getStartTime());
        schedule.setEndTime(dto.getEndTime());
        schedule.setCapacity(dto.getCapacity());

        GroupClassSchedule savedSchedule = groupClassScheduleRepository.save(schedule);

        // Tworzenie sesji treningowej dla trenera (bez użytkownika, bo to zajęcia grupowe)
        TrainingSession session = new TrainingSession();
        session.setTrainerId(dto.getTrainerId());
        session.setUserId(null);
        session.setStartTime(dto.getStartTime());
        session.setEndTime(dto.getEndTime());
        session.setClassId(savedSchedule.getId());
        session.setType(SessionType.GROUP);
        session.setStatus(SessionStatus.CONFIRMED);

        trainingSessionService.createGroupTrainingSession(session);

        return mapToDto(savedSchedule);
    }

    public GroupClassScheduleDTO mapToDto(GroupClassSchedule schedule) {
        GroupClassScheduleDTO dto = new GroupClassScheduleDTO();
        dto.setId(schedule.getId());
        dto.setGymGroupClassOfferingName(schedule.getGymGroupClassOffering().getGroupClassDefinition().getName()); // zakładam, że ma pole 'name'
        dto.setTrainerId(schedule.getTrainerId());
        dto.setStartTime(schedule.getStartTime());
        dto.setEndTime(schedule.getEndTime());
        dto.setCapacity(schedule.getCapacity());
        dto.setParticipantIds(schedule.getParticipantIds());
        return dto;
    }

    @Transactional
    public GroupClassScheduleDTO signUpUser(Long scheduleId, Long userId) {
        GroupClassSchedule schedule = groupClassScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new NotFoundException("Schedule not found"));

        if (schedule.getParticipantIds().contains(userId)) {
            throw new IllegalStateException("User already signed up");
        }

        if (schedule.getParticipantIds().size() >= schedule.getCapacity()) {
            throw new IllegalStateException("Schedule is full");
        }

        try {
            trainingSessionService.checkUserAvailability(userId, schedule.getStartTime(), schedule.getEndTime());
        } catch (ScheduleConflictException e) {
            throw new IllegalStateException("User has another session at this time.");
        }

        schedule.getParticipantIds().add(userId);
        GroupClassSchedule savedSchedule = groupClassScheduleRepository.save(schedule);

        TrainingSession session = new TrainingSession();
        session.setTrainerId(schedule.getTrainerId());
        session.setUserId(userId);
        session.setStartTime(schedule.getStartTime());
        session.setEndTime(schedule.getEndTime());
        session.setClassId(savedSchedule.getId());
        session.setType(SessionType.GROUP);
        session.setStatus(SessionStatus.CONFIRMED);

        trainingSessionService.createGroupTrainingSession(session);

        return mapToDto(savedSchedule);
    }

    public Mono<List<UserProfileResponseDTO>> getParticipantsProfiles(Long scheduleId, String authHeader) {
        GroupClassSchedule schedule = groupClassScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found"));

        List<Long> participantIds = schedule.getParticipantIds();

        if (participantIds.isEmpty()) {
            return Mono.just(Collections.emptyList());
        }

        return Flux.fromIterable(participantIds)
                .flatMap(userId -> Mono.fromCallable(() -> userServiceClient.getProfile(authHeader, userId))
                        .map(response -> new UserProfileResponseDTO(
                                response.getId(),
                                response.getUserId(),
                                response.getFirstName(),
                                response.getLastName(),
                                response.getGender(),
                                response.getHeight(),
                                response.getWeight(),
                                response.getBirthday(),
                                response.getPhone(),
                                response.getLevel() != null ? response.getLevel().toString() : null,
                                response.getBio(),
                                response.getAvatarUrl(),
                                response.getRoles()
                        ))
                        .onErrorResume(e -> Mono.empty())
                )
                .collectList();
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

    public List<GroupClassScheduleDTO> getGroupClassesByGymId(Long gymId) {
        return groupClassScheduleRepository.findAll().stream()
                .filter(schedule -> schedule.getGymGroupClassOffering().getGym().getId().equals(gymId))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
}
