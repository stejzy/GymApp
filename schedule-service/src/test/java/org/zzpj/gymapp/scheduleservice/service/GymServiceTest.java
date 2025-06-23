package org.zzpj.gymapp.scheduleservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zzpj.gymapp.scheduleservice.client.UserServiceClient;
import org.zzpj.gymapp.scheduleservice.dto.*;
import org.zzpj.gymapp.scheduleservice.exeption.CoachAlreadyAssignedException;
import org.zzpj.gymapp.scheduleservice.exeption.GymNotFoundException;
import org.zzpj.gymapp.scheduleservice.exeption.UserNotCoachException;
import org.zzpj.gymapp.scheduleservice.exeption.UserNotFoundException;
import org.zzpj.gymapp.scheduleservice.model.Gym;
import org.zzpj.gymapp.scheduleservice.model.GymGroupClassOffering;
import org.zzpj.gymapp.scheduleservice.repository.GymGroupClassOfferingRepository;
import org.zzpj.gymapp.scheduleservice.repository.GymRepository;
import reactor.test.StepVerifier;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class GymServiceTest {
    @Mock
    private GymRepository gymRepository;
    @Mock
    private GymGroupClassOfferingRepository gymGroupClassOfferingRepository;
    @Mock
    private GymGroupClassOfferingService gymGroupClassOfferingService;
    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private GymService gymService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        gymService = new GymService(gymRepository, gymGroupClassOfferingRepository, gymGroupClassOfferingService,
                userServiceClient);
    }

    @Test
    void getAllGyms_shouldReturnList() {
        List<Gym> gyms = List.of(
                new Gym(1L, "A", "C", "Addr", "123", LocalTime.of(6,0),
                        LocalTime.of(22,0), List.of(1L), List.of()),
                new Gym(2L, "B", "C", "Addr2", "456", LocalTime.of(7,0),
                        LocalTime.of(21,0), List.of(2L), List.of())
        );
        when(gymRepository.findAll()).thenReturn(gyms);
        List<GymDTO> result = gymService.getAllGyms();
        assertThat(result).hasSize(2);
        assertThat(result.getFirst().name()).isEqualTo("A");
    }

    @Test
    void getGymById_shouldReturnGym() {
        Gym gym = new Gym(1L, "A", "C", "Addr", "123", LocalTime.of(6,0),
                LocalTime.of(22,0), List.of(1L), List.of());
        when(gymRepository.findById(1L)).thenReturn(Optional.of(gym));
        GymDTO dto = gymService.getGymById(1L);
        assertThat(dto.name()).isEqualTo("A");
    }

    @Test
    void getGymById_shouldThrowIfNotFound() {
        when(gymRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> gymService.getGymById(99L)).isInstanceOf(jakarta.persistence.EntityNotFoundException.class);
    }

    @Test
    void getGymGroupClassOfferings_shouldReturnList() {
        Gym gym = new Gym(1L, "A", "C", "Addr", "123", LocalTime.of(6,0),
                LocalTime.of(22,0), List.of(1L), List.of());
        GymGroupClassOffering offering = new GymGroupClassOffering();
        offering.setId(10L);
        offering.setGym(gym);
        when(gymRepository.findById(1L)).thenReturn(Optional.of(gym));
        when(gymGroupClassOfferingRepository.findByGym(gym)).thenReturn(List.of(offering));
        GymGroupClassOfferingDTO dto = new GymGroupClassOfferingDTO(10L, null, null);
        when(gymGroupClassOfferingService.mapToDto(offering)).thenReturn(dto);
        List<GymGroupClassOfferingDTO> result = gymService.getGymGroupClassOfferings(1L);
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo(10L);
    }

    @Test
    void getGymGroupClassOfferings_shouldThrowIfNotFound() {
        when(gymRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> gymService.getGymGroupClassOfferings(99L)).isInstanceOf(jakarta.persistence.EntityNotFoundException.class);
    }

    @Test
    void addTrainerToGym_shouldAddTrainerSuccessfully() {
        Gym gym = new Gym(1L, "A", "C", "Addr", "123", LocalTime.of(6,0),
                LocalTime.of(22,0), new java.util.ArrayList<>(), List.of());
        when(gymRepository.findById(1L)).thenReturn(Optional.of(gym));
        UserProfileResponse userProfile = new UserProfileResponse(1L, 2L, "Jan", "Kowalski",
                "M", 180.0, 80.0, null, "123", null, "bio", null, Set.of("COACH"));
        when(userServiceClient.getProfile("auth", 2L)).thenReturn(userProfile);
        when(gymRepository.findAll()).thenReturn(List.of(gym));
        StepVerifier.create(gymService.addTrainerToGym(1L, 2L, "auth"))
                .expectNextMatches(resp ->
                        resp.getUserId().equals(2L)
                                && resp.getFirstName().equals("Jan")
                                && resp.getRoles().contains("COACH")
                )
                .verifyComplete();
        assertThat(gym.getTrainerIds()).contains(2L);
    }

    @Test
    void addTrainerToGym_shouldThrowIfUserNotCoach() {
        Gym gym = new Gym(1L, "A", "C", "Addr", "123", LocalTime.of(6,0),
                LocalTime.of(22,0), new java.util.ArrayList<>(), List.of());
        when(gymRepository.findById(1L)).thenReturn(Optional.of(gym));
        UserProfileResponse userProfile = new UserProfileResponse(1L, 2L, "Jan", "Kowalski",
                "M", 180.0, 80.0, null, "123", null, "bio", null, Set.of("USER"));
        when(userServiceClient.getProfile("auth", 2L)).thenReturn(userProfile);
        StepVerifier.create(gymService.addTrainerToGym(1L, 2L, "auth"))
                .expectErrorSatisfies(e -> assertThat(e).isInstanceOf(UserNotCoachException.class))
                .verify();
    }

    @Test
    void addTrainerToGym_shouldThrowIfTrainerAlreadyAssignedToThisGym() {
        Gym gym = new Gym(1L, "A", "C", "Addr", "123", LocalTime.of(6,0),
                LocalTime.of(22,0), new java.util.ArrayList<>(List.of(2L)), List.of());
        when(gymRepository.findById(1L)).thenReturn(Optional.of(gym));
        UserProfileResponse userProfile = new UserProfileResponse(1L, 2L, "Jan", "Kowalski",
                "M", 180.0, 80.0, null, "123", null, "bio", null, Set.of("COACH"));
        when(userServiceClient.getProfile("auth", 2L)).thenReturn(userProfile);
        when(gymRepository.findAll()).thenReturn(List.of(gym));
        StepVerifier.create(gymService.addTrainerToGym(1L, 2L, "auth"))
                .expectErrorSatisfies(e -> assertThat(e).isInstanceOf(CoachAlreadyAssignedException.class))
                .verify();
    }

    @Test
    void addTrainerToGym_shouldThrowIfTrainerAssignedElsewhere() {
        Gym gym1 = new Gym(1L, "A", "C", "Addr", "123", LocalTime.of(6,0),
                LocalTime.of(22,0), new java.util.ArrayList<>(), List.of());
        Gym gym2 = new Gym(2L, "B", "C", "Addr2", "456", LocalTime.of(7,0),
                LocalTime.of(21,0), new java.util.ArrayList<>(List.of(2L)), List.of());
        when(gymRepository.findById(1L)).thenReturn(Optional.of(gym1));
        UserProfileResponse userProfile = new UserProfileResponse(1L, 2L, "Jan", "Kowalski",
                "M", 180.0, 80.0, null, "123", null, "bio", null, Set.of("COACH"));
        when(userServiceClient.getProfile("auth", 2L)).thenReturn(userProfile);
        when(gymRepository.findAll()).thenReturn(List.of(gym1, gym2));
        StepVerifier.create(gymService.addTrainerToGym(1L, 2L, "auth"))
                .expectErrorSatisfies(e -> assertThat(e).isInstanceOf(CoachAlreadyAssignedException.class))
                .verify();
    }

    @Test
    void addTrainerToGym_shouldThrowIfUserNotFound() {
        Gym gym = new Gym(1L, "A", "C", "Addr", "123", LocalTime.of(6,0),
                LocalTime.of(22,0), new java.util.ArrayList<>(), List.of());
        when(gymRepository.findById(1L)).thenReturn(Optional.of(gym));
        when(userServiceClient.getProfile("auth", 2L)).thenThrow(new UserNotFoundException("not found"));
        StepVerifier.create(gymService.addTrainerToGym(1L, 2L, "auth"))
                .expectErrorSatisfies(e -> assertThat(e).isInstanceOf(UserNotFoundException.class))
                .verify();
    }

    @Test
    void getTrainersByGymId_shouldReturnTrainerList() {
        Gym gym = new Gym(1L, "A", "C", "Addr", "123", LocalTime.of(6,0),
                LocalTime.of(22,0), List.of(2L), List.of());
        when(gymRepository.findById(1L)).thenReturn(Optional.of(gym));
        UserProfileResponse userProfile = new UserProfileResponse(1L, 2L, "Jan", "Kowalski", "M",
                180.0, 80.0, null, "123", null, "bio", null, Set.of("COACH"));
        when(userServiceClient.getProfile("auth", 2L)).thenReturn(userProfile);
        StepVerifier.create(gymService.getTrainersByGymId(1L, "auth"))
                .expectNextMatches(trainer ->
                        trainer.getId().equals(1L) &&
                                trainer.getUserId().equals(2L) &&
                                trainer.getFirstName().equals("Jan") &&
                                trainer.getRoles().contains("COACH") &&
                                trainer.getRoles().size() == 1
                ).verifyComplete();
    }

    @Test
    void getTrainersByGymId_shouldReturnEmptyIfNoTrainers() {
        Gym gym = new Gym(1L, "A", "C", "Addr", "123", LocalTime.of(6,0),
                LocalTime.of(22,0), List.of(), List.of());
        when(gymRepository.findById(1L)).thenReturn(Optional.of(gym));
        StepVerifier.create(gymService.getTrainersByGymId(1L, "auth"))
                .verifyComplete();
    }

    @Test
    void getTrainersByGymId_shouldSkipTrainerIfProfileError() {
        Gym gym = new Gym(1L, "A", "C", "Addr", "123", LocalTime.of(6,0),
                LocalTime.of(22,0), List.of(2L, 3L), List.of());
        when(gymRepository.findById(1L)).thenReturn(Optional.of(gym));
        UserProfileResponse userProfile = new UserProfileResponse(1L, 2L, "Jan", "Kowalski", "M",
                180.0, 80.0, null, "123", null, "bio", null, Set.of("COACH"));
        when(userServiceClient.getProfile("auth", 2L)).thenReturn(userProfile);
        when(userServiceClient.getProfile("auth", 3L)).thenThrow(new RuntimeException("error"));
        StepVerifier.create(gymService.getTrainersByGymId(1L, "auth"))
                .expectNextMatches(trainer ->
                        trainer.getUserId().equals(2L)
                                && trainer.getFirstName().equals("Jan")
                                && trainer.getRoles().contains("COACH")
                                && trainer.getRoles().size() == 1
                ).verifyComplete();
    }

    @Test
    void getTrainersByGymId_shouldThrowIfGymNotFound() {
        when(gymRepository.findById(99L)).thenReturn(Optional.empty());
        StepVerifier.create(gymService.getTrainersByGymId(99L, "auth"))
                .expectErrorSatisfies(e -> assertThat(e).isInstanceOf(GymNotFoundException.class))
                .verify();
    }
} 