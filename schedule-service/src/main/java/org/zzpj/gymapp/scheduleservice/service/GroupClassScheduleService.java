package org.zzpj.gymapp.scheduleservice.service;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.zzpj.gymapp.scheduleservice.dto.CreateGroupClassScheduleDTO;
import org.zzpj.gymapp.scheduleservice.dto.GroupClassScheduleDTO;
import org.zzpj.gymapp.scheduleservice.dto.UserProfileResponseDTO;
import org.zzpj.gymapp.scheduleservice.exeption.ScheduleConflictException;
import org.zzpj.gymapp.scheduleservice.exeption.TrainerBusyException;
import org.zzpj.gymapp.scheduleservice.model.GroupClassSchedule;
import org.zzpj.gymapp.scheduleservice.model.GymGroupClassOffering;
import org.zzpj.gymapp.scheduleservice.model.SessionType;
import org.zzpj.gymapp.scheduleservice.model.TrainingSession;
import org.zzpj.gymapp.scheduleservice.repository.GroupClassScheduleRepository;
import org.zzpj.gymapp.scheduleservice.repository.GymGroupClassOfferingRepository;
import org.zzpj.gymapp.scheduleservice.repository.TrainingSessionRepository;
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
    private final WebClient authServiceClient;

    public GroupClassScheduleService(GroupClassScheduleRepository groupClassScheduleRepository,
                                     TrainingSessionRepository trainingSessionRepository,
                                     TrainingSessionService trainingSessionService,
                                     GymGroupClassOfferingRepository gymGroupClassOfferingRepository,
                                     WebClient.Builder webClientBuilder,
                                     @Value("${user.base-url}") String authBaseUrl) {
        this.groupClassScheduleRepository = groupClassScheduleRepository;
        this.trainingSessionService = trainingSessionService;
        this.gymGroupClassOfferingRepository = gymGroupClassOfferingRepository;
        this.authServiceClient = webClientBuilder.baseUrl(authBaseUrl).build();
    }

    @Transactional
    public GroupClassScheduleDTO  addGroupClassSchedule(CreateGroupClassScheduleDTO dto) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneMonthAhead = now.plusMonths(1);

        System.out.println(dto);

        if (dto.getStartTime().isAfter(oneMonthAhead)) {
            throw new IllegalArgumentException("Zajęcia mogą być tworzone maksymalnie miesiąc do przodu.");
        }

        System.out.println("SKIBIFI");

        GymGroupClassOffering gymGroupClassOffering = gymGroupClassOfferingRepository
                .findById(dto.getGymGroupClassOfferingId())
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono oferty grupowych zajęć."));

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

        trainingSessionService.createTrainingSession(session);

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

        trainingSessionService.createTrainingSession(session);

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
                .flatMap(userId -> authServiceClient.get()
                        .uri("/profile/{userId}", userId)
                        .header(HttpHeaders.AUTHORIZATION, authHeader)
                        .retrieve()
                        .bodyToMono(UserProfileResponseDTO.class)
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
}
