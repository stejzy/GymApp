package org.zzpj.gymapp.scheduleservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zzpj.gymapp.scheduleservice.client.UserServiceClient;
import org.zzpj.gymapp.scheduleservice.dto.CreateGroupClassScheduleDTO;
import org.zzpj.gymapp.scheduleservice.dto.GroupClassScheduleDTO;
import org.zzpj.gymapp.scheduleservice.exeption.ScheduleConflictException;
import org.zzpj.gymapp.scheduleservice.exeption.TrainerBusyException;
import org.zzpj.gymapp.scheduleservice.exeption.TrainerNotAssignedToGymException;
import org.zzpj.gymapp.scheduleservice.model.*;
import org.zzpj.gymapp.scheduleservice.repository.GroupClassScheduleRepository;
import org.zzpj.gymapp.scheduleservice.repository.GymGroupClassOfferingRepository;
import org.zzpj.gymapp.scheduleservice.repository.TrainingSessionRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class GroupClassScheduleServiceTest {
    @Mock
    private GroupClassScheduleRepository groupClassScheduleRepository;
    @Mock
    private TrainingSessionService trainingSessionService;
    @Mock
    private GymGroupClassOfferingRepository gymGroupClassOfferingRepository;
    @Mock
    private UserServiceClient userServiceClient;
    @Mock
    private TrainingSessionRepository trainingSessionRepository;

    @InjectMocks
    private GroupClassScheduleService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new GroupClassScheduleService(
                groupClassScheduleRepository,
                trainingSessionRepository,
                trainingSessionService,
                gymGroupClassOfferingRepository,
                userServiceClient
        );
    }

    @Test
    void addGroupClassSchedule_shouldThrowIfTooEarly() {
        CreateGroupClassScheduleDTO dto = new CreateGroupClassScheduleDTO();
        dto.setStartTime(LocalDateTime.now());
        dto.setEndTime(LocalDateTime.now().plusHours(1));
        assertThatThrownBy(() -> service.addGroupClassSchedule(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("jednodniowym wyprzedzeniem");
    }

    @Test
    void addGroupClassSchedule_shouldThrowIfTooLate() {
        CreateGroupClassScheduleDTO dto = new CreateGroupClassScheduleDTO();
        dto.setStartTime(LocalDateTime.now().plusMonths(2));
        dto.setEndTime(LocalDateTime.now().plusMonths(2).plusHours(1));
        assertThatThrownBy(() -> service.addGroupClassSchedule(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("miesiąc do przodu");
    }

    @Test
    void addGroupClassSchedule_shouldThrowIfTrainerNotAssigned() {
        CreateGroupClassScheduleDTO dto = new CreateGroupClassScheduleDTO();
        dto.setStartTime(LocalDateTime.now().plusDays(2));
        dto.setEndTime(LocalDateTime.now().plusDays(2).plusHours(1));
        dto.setGymGroupClassOfferingId(1L);
        dto.setTrainerId(5L);
        GymGroupClassOffering offering = mock(GymGroupClassOffering.class);
        Gym gym = new Gym();
        gym.setTrainerIds(Collections.emptyList());
        when(gymGroupClassOfferingRepository.findById(1L)).thenReturn(Optional.of(offering));
        when(offering.getGym()).thenReturn(gym);
        assertThatThrownBy(() -> service.addGroupClassSchedule(dto))
                .isInstanceOf(TrainerNotAssignedToGymException.class);
    }

    @Test
    void addGroupClassSchedule_shouldThrowIfConflict() {
        CreateGroupClassScheduleDTO dto = new CreateGroupClassScheduleDTO();
        dto.setStartTime(LocalDateTime.now().plusDays(2));
        dto.setEndTime(LocalDateTime.now().plusDays(2).plusHours(1));
        dto.setGymGroupClassOfferingId(1L);
        dto.setTrainerId(5L);
        GymGroupClassOffering offering = mock(GymGroupClassOffering.class);
        Gym gym = new Gym();
        gym.setTrainerIds(List.of(5L));
        gym.setId(10L);
        when(gymGroupClassOfferingRepository.findById(1L)).thenReturn(Optional.of(offering));
        when(offering.getGym()).thenReturn(gym);
        when(groupClassScheduleRepository.findConflictingGroupSchedules(eq(10L), any(), any())).thenReturn(List.of(new GroupClassSchedule()));
        assertThatThrownBy(() -> service.addGroupClassSchedule(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Kolizja");
    }

    @Test
    void addGroupClassSchedule_shouldThrowIfTrainerBusy() {
        CreateGroupClassScheduleDTO dto = new CreateGroupClassScheduleDTO();
        dto.setStartTime(LocalDateTime.now().plusDays(2));
        dto.setEndTime(LocalDateTime.now().plusDays(2).plusHours(1));
        dto.setGymGroupClassOfferingId(1L);
        dto.setTrainerId(5L);
        GymGroupClassOffering offering = mock(GymGroupClassOffering.class);
        Gym gym = new Gym();
        gym.setTrainerIds(List.of(5L));
        gym.setId(10L);
        when(gymGroupClassOfferingRepository.findById(1L)).thenReturn(Optional.of(offering));
        when(offering.getGym()).thenReturn(gym);
        when(groupClassScheduleRepository.findConflictingGroupSchedules(eq(10L), any(), any())).thenReturn(Collections.emptyList());
        doThrow(new ScheduleConflictException("Trainer busy")).when(trainingSessionService).checkTrainerAvailability(any(), any(), any());
        assertThatThrownBy(() -> service.addGroupClassSchedule(dto))
                .isInstanceOf(TrainerBusyException.class);
    }

    @Test
    void signUpUser_shouldThrowIfUserAlreadySignedUp() {
        GroupClassSchedule schedule = new GroupClassSchedule();
        schedule.setId(1L);
        schedule.setParticipantIds(List.of(2L));
        schedule.setCapacity(10);
        when(groupClassScheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
        assertThatThrownBy(() -> service.signUpUser(1L, 2L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already signed up");
    }

    @Test
    void signUpUser_shouldThrowIfScheduleIsFull() {
        GroupClassSchedule schedule = new GroupClassSchedule();
        schedule.setId(1L);
        schedule.setParticipantIds(List.of(2L, 3L));
        schedule.setCapacity(2);
        when(groupClassScheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
        assertThatThrownBy(() -> service.signUpUser(1L, 4L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("full");
    }

    @Test
    void signUpUser_shouldThrowIfUserHasConflict() {
        GroupClassSchedule schedule = new GroupClassSchedule();
        schedule.setId(1L);
        schedule.setParticipantIds(List.of());
        schedule.setCapacity(2);
        schedule.setStartTime(LocalDateTime.now().plusDays(1));
        schedule.setEndTime(LocalDateTime.now().plusDays(1).plusHours(1));
        when(groupClassScheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
        doThrow(new ScheduleConflictException("User busy")).when(trainingSessionService).checkUserAvailability(any(), any(), any());
        assertThatThrownBy(() -> service.signUpUser(1L, 5L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another session");
    }

    @Test
    void signUpUser_shouldAddUserIfPossible() {
        GroupClassDefinition def = new GroupClassDefinition();
        def.setName("TestClass");
        GymGroupClassOffering offering = mock(GymGroupClassOffering.class);
        when(offering.getGroupClassDefinition()).thenReturn(def);

        GroupClassSchedule schedule = new GroupClassSchedule();
        schedule.setId(1L);
        schedule.setParticipantIds(new java.util.ArrayList<>());
        schedule.setCapacity(2);
        schedule.setStartTime(LocalDateTime.now().plusDays(1));
        schedule.setEndTime(LocalDateTime.now().plusDays(1).plusHours(1));
        schedule.setTrainerId(10L);
        schedule.setGymGroupClassOffering(offering);
        when(groupClassScheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
        when(groupClassScheduleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(trainingSessionService).checkUserAvailability(any(), any(), any());
        when(trainingSessionService.createGroupTrainingSession(any())).thenReturn(new TrainingSession());
        GroupClassScheduleDTO dto = service.signUpUser(1L, 5L);
        assertThat(dto.getParticipantIds()).contains(5L);
    }
} 