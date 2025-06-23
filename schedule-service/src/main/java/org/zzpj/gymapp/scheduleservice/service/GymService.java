package org.zzpj.gymapp.scheduleservice.service;

import feign.FeignException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.zzpj.gymapp.scheduleservice.client.UserServiceClient;
import org.zzpj.gymapp.scheduleservice.dto.*;
import org.zzpj.gymapp.scheduleservice.exeption.CoachAlreadyAssignedException;
import org.zzpj.gymapp.scheduleservice.exeption.GymNotFoundException;
import org.zzpj.gymapp.scheduleservice.exeption.UserNotCoachException;
import org.zzpj.gymapp.scheduleservice.exeption.UserNotFoundException;
import org.zzpj.gymapp.scheduleservice.model.Gym;
import org.zzpj.gymapp.scheduleservice.repository.GymGroupClassOfferingRepository;
import org.zzpj.gymapp.scheduleservice.repository.GymRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;

@Service
public class GymService {

    private final GymRepository gymRepository;
    private final GymGroupClassOfferingRepository gymGroupClassOfferingRepository;
    private final GymGroupClassOfferingService gymGroupClassOfferingService;
    private final UserServiceClient userServiceClient;

    public GymService(GymRepository gymRepository,
                      GymGroupClassOfferingRepository gymGroupClassOfferingRepository,
                      GymGroupClassOfferingService gymGroupClassOfferingService,
                      UserServiceClient userServiceClient)
    {
        this.gymRepository = gymRepository;
        this.gymGroupClassOfferingRepository = gymGroupClassOfferingRepository;
        this.gymGroupClassOfferingService = gymGroupClassOfferingService;
        this.userServiceClient = userServiceClient;
    }

    public List<GymDTO> getAllGyms() {
        return gymRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public GymDTO getGymById(Long id) {
        return gymRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new EntityNotFoundException("Gym with ID " + id + " not found"));
    }

    public List<GymGroupClassOfferingDTO> getGymGroupClassOfferings(Long gymId) {
        Gym gym = gymRepository.findById(gymId)
                .orElseThrow(() -> new EntityNotFoundException("Gym with ID " + gymId + " not found"));

        return gymGroupClassOfferingRepository.findByGym(gym)
                .stream()
                .map(gymGroupClassOfferingService::mapToDto
                )
                .toList();
    }

    private GymDTO mapToDTO(Gym gym) {
        return new GymDTO(
                gym.getId(),
                gym.getName(),
                gym.getCity(),
                gym.getAddress(),
                gym.getPhoneNumber(),
                gym.getOpeningHour(),
                gym.getClosingHour()
        );
    }

    public Mono<UserProfileResponseDTO> addTrainerToGym(Long gymId, Long userId, String authHeader) {
        Mono<Gym> gymMono = Mono.fromCallable(() -> gymRepository.findById(gymId)
                        .orElseThrow(() -> new GymNotFoundException("Siłownia o podanym ID nie istnieje.")))
                .subscribeOn(Schedulers.boundedElastic());

        Mono<UserProfileResponseDTO> userProfileMono = Mono.fromCallable(() -> {
                    UserProfileResponse response = userServiceClient.getProfile(authHeader, userId);
                    System.out.println("ELO");
                    System.out.println(response.getUserId());
                    return new UserProfileResponseDTO(
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
                    );
                })
                .onErrorResume(ex -> {
                    if (ex instanceof FeignException.NotFound) {
                        return Mono.error(new UserNotFoundException("Użytkownik o podanym ID nie istnieje."));
                    } else if (ex instanceof FeignException) {
                        return Mono.error(new RuntimeException("Serwis user-service zwrócił błąd."));
                    }
                    return Mono.error(ex);
                })
                .subscribeOn(Schedulers.boundedElastic());

        return Mono.zip(gymMono, userProfileMono)
                .flatMap(tuple -> {
                    Gym gym = tuple.getT1();
                    UserProfileResponseDTO userProfile = tuple.getT2();

                    if (!userProfile.getRoles().contains("COACH")) {
                        return Mono.error(new UserNotCoachException("Użytkownik nie ma roli trenera."));
                    }

                    return Mono.fromCallable(() -> {
                                if (gym.getTrainerIds().contains(userId)) {
                                    throw new CoachAlreadyAssignedException("Trener jest już przypisany do tej siłowni.");
                                }

                                boolean assignedElsewhere = gymRepository.findAll().stream()
                                        .anyMatch(g -> !g.getId().equals(gym.getId()) && g.getTrainerIds().contains(userId));
                                if (assignedElsewhere) {
                                    throw new CoachAlreadyAssignedException("Trener jest już przypisany do innej siłowni.");
                                }

                                gym.getTrainerIds().add(userId);
                                gymRepository.save(gym);
                                return userProfile;
                            })
                            .subscribeOn(Schedulers.boundedElastic());
                });
    }



    public Flux<TrainerSummaryDTO> getTrainersByGymId(Long gymId, String authHeader) {
        Mono<Gym> gymMono = Mono.fromCallable(() -> gymRepository.findById(gymId)
                        .orElseThrow(() -> new GymNotFoundException("Siłownia o podanym ID nie istnieje.")))
                .subscribeOn(Schedulers.boundedElastic());

        return gymMono.flatMapMany(gym -> {
            List<Long> trainerIds = gym.getTrainerIds();
            if (trainerIds.isEmpty()) {
                return Flux.empty();
            }

            return Flux.fromIterable(trainerIds)
                    .flatMap(userId -> Mono.fromCallable(() -> {
                                        UserProfileResponse response = userServiceClient.getProfile(authHeader, userId);
                                        return new TrainerSummaryDTO(
                                                response.getId(),
                                                response.getUserId(),
                                                response.getFirstName(),
                                                response.getLastName(),
                                                response.getPhone(),
                                                response.getBio(),
                                                new ArrayList<>(response.getRoles())
                                        );
                                    })
                                    .onErrorResume(e -> Mono.empty()) // Pomija trenerów, których nie można pobrać
                                    .subscribeOn(Schedulers.boundedElastic())
                    );
        });
    }


}
