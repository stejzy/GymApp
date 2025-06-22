package org.zzpj.gymapp.scheduleservice.controller;

import jakarta.ws.rs.Path;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.zzpj.gymapp.scheduleservice.dto.GymDTO;
import org.zzpj.gymapp.scheduleservice.dto.GymGroupClassOfferingDTO;
import org.zzpj.gymapp.scheduleservice.dto.TrainerSummaryDTO;
import org.zzpj.gymapp.scheduleservice.dto.UserProfileResponseDTO;
import org.zzpj.gymapp.scheduleservice.model.Gym;
import org.zzpj.gymapp.scheduleservice.service.GymService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/gyms")
public class GymController {

    private final GymService gymService;

    public GymController(GymService gymService) {
        this.gymService = gymService;
    }

    @GetMapping("")
    public ResponseEntity<List<GymDTO>> getAllGyms() {
        return ResponseEntity.ok(gymService.getAllGyms());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GymDTO> getGymById(@PathVariable Long id) {
        return ResponseEntity.ok(gymService.getGymById(id));
    }

    @GetMapping("/{id}/group-class-offerings")
    public ResponseEntity<List<GymGroupClassOfferingDTO>> getGymGroupClassOfferings(@PathVariable Long id) {
        return ResponseEntity.ok(gymService.getGymGroupClassOfferings(id));
    }

    @PostMapping("/{gymId}/add-trainer/{userId}")
    public Mono<ResponseEntity<UserProfileResponseDTO>> addTrainerToGym(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @PathVariable Long gymId,
            @PathVariable Long userId) {
        return gymService.addTrainerToGym(gymId, userId, authHeader)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{gymId}/trainers")
    public Flux<TrainerSummaryDTO> getTrainersByGym(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @PathVariable Long gymId) {
        return gymService.getTrainersByGymId(gymId, authHeader);
    }




}
