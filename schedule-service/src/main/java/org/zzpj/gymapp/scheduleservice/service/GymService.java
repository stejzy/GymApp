package org.zzpj.gymapp.scheduleservice.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.zzpj.gymapp.scheduleservice.dto.GymDTO;
import org.zzpj.gymapp.scheduleservice.dto.GymGroupClassOfferingDTO;
import org.zzpj.gymapp.scheduleservice.model.Gym;
import org.zzpj.gymapp.scheduleservice.repository.GymGroupClassOfferingRepository;
import org.zzpj.gymapp.scheduleservice.repository.GymRepository;

import java.util.List;

@Service
public class GymService {

    private final GymRepository gymRepository;
    private final GymGroupClassOfferingRepository gymGroupClassOfferingRepository;
    private final GymGroupClassOfferingService gymGroupClassOfferingService;

    public GymService(GymRepository gymRepository,
                      GymGroupClassOfferingRepository gymGroupClassOfferingRepository,
                      GymGroupClassOfferingService gymGroupClassOfferingService) {
        this.gymRepository = gymRepository;
        this.gymGroupClassOfferingRepository = gymGroupClassOfferingRepository;
        this.gymGroupClassOfferingService = gymGroupClassOfferingService;
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

}
