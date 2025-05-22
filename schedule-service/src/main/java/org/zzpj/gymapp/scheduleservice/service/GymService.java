package org.zzpj.gymapp.scheduleservice.service;

import org.antlr.v4.runtime.misc.MultiMap;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.zzpj.gymapp.scheduleservice.dto.GymDTO;
import org.zzpj.gymapp.scheduleservice.exeption.GymNotFoundException;
import org.zzpj.gymapp.scheduleservice.model.Gym;
import org.zzpj.gymapp.scheduleservice.repository.GymRepository;

import java.util.List;

@Service
public class GymService {

    private final GymRepository gymRepository;

    public GymService(GymRepository gymRepository) {
        this.gymRepository = gymRepository;
    }

    public List<GymDTO> getAllGyms() {
        return gymRepository.findAll()
                .stream()
                .map(this::mapToDTO)
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

    public GymDTO getGymById(Long id) {
        return gymRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new GymNotFoundException("Gym not found"));
    }
}
