package org.zzpj.gymapp.scheduleservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrainingSessionServiceTest {
    @Mock
    private TrainingSessionRepository trainingSessionRepository;

    @InjectMocks
    private TrainingSessionService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new TrainingSessionService(trainingSessionRepository);
    }

    @Test
    void checkTrainerAvailability_shouldThrowIfConflict() {
        when(trainingSessionRepository.findTrainerConflicts(any(), any(), any())).thenReturn(List.of(new TrainingSession()));
        assertThatThrownBy(() -> service.checkTrainerAvailability(1L, LocalDateTime.now(), LocalDateTime.now().plusHours(1)))
                .isInstanceOf(ScheduleConflictException.class);
    }

    @Test
    void checkUserAvailability_shouldThrowIfConflict() {
        when(trainingSessionRepository.findUserConflicts(any(), any(), any())).thenReturn(List.of(new TrainingSession()));
        assertThatThrownBy(() -> service.checkUserAvailability(1L, LocalDateTime.now(), LocalDateTime.now().plusHours(1)))
                .isInstanceOf(ScheduleConflictException.class);
    }

    @Test
    void createGroupTrainingSession_shouldThrowIfInvalidTimes() {
        TrainingSession session = new TrainingSession();
        session.setTrainerId(1L);
        session.setStartTime(LocalDateTime.now().plusHours(2));
        session.setEndTime(LocalDateTime.now().plusHours(1));
        assertThatThrownBy(() -> service.createGroupTrainingSession(session))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Start time must be before end time");
    }

    @Test
    void createIndividualTrainingSession_shouldThrowIfCoach() {
        assertThatThrownBy(() -> service.createIndividualTrainingSession(1L, LocalDate.now(), LocalTime.now(), LocalTime.now().plusHours(1), 2L, "COACH"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void respondToIndividualSession_shouldThrowIfNotAssignedTrainer() {
        TrainingSession session = new TrainingSession();
        session.setId(1L);
        session.setTrainerId(2L);
        session.setType(SessionType.INDIVIDUAL_COACH);
        session.setStatus(SessionStatus.STAGING);
        when(trainingSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        assertThatThrownBy(() -> service.respondToIndividualSession(1L, 3L, true))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void createGroupTrainingSession_shouldSaveIfValid() {
        TrainingSession session = new TrainingSession();
        session.setTrainerId(1L);
        session.setStartTime(LocalDateTime.now());
        session.setEndTime(LocalDateTime.now().plusHours(1));
        when(trainingSessionRepository.findTrainerConflicts(any(), any(), any())).thenReturn(List.of());
        when(trainingSessionRepository.save(any())).thenReturn(session);
        TrainingSession saved = service.createGroupTrainingSession(session);
        assertThat(saved).isNotNull();
    }

    @Test
    void createIndividualTrainingSession_shouldSaveIfValid() {
        when(trainingSessionRepository.findTrainerConflicts(any(), any(), any())).thenReturn(List.of());
        when(trainingSessionRepository.findUserConflicts(any(), any(), any())).thenReturn(List.of());
        when(trainingSessionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        TrainingSession session = service.createIndividualTrainingSession(1L, LocalDate.now(), LocalTime.of(10,0), LocalTime.of(11,0), 2L, "USER");
        assertThat(session).isNotNull();
        assertThat(session.getTrainerId()).isEqualTo(1L);
        assertThat(session.getUserId()).isEqualTo(2L);
    }

    @Test
    void getById_shouldReturnSessionIfExists() {
        TrainingSession session = new TrainingSession();
        session.setId(1L);
        when(trainingSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        TrainingSession found = service.getById(1L);
        assertThat(found.getId()).isEqualTo(1L);
    }

    @Test
    void getById_shouldThrowIfNotExists() {
        when(trainingSessionRepository.findById(2L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getById(2L)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deleteSession_shouldCallRepository() {
        doNothing().when(trainingSessionRepository).deleteById(1L);
        service.deleteSession(1L);
        verify(trainingSessionRepository, times(1)).deleteById(1L);
    }
} 