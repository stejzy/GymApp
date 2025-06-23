package org.zzpj.gymapp.scheduleservice.controller;

import jakarta.ws.rs.Path;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.zzpj.gymapp.scheduleservice.dto.*;
import org.zzpj.gymapp.scheduleservice.model.Gym;
import org.zzpj.gymapp.scheduleservice.service.GroupClassScheduleService;
import org.zzpj.gymapp.scheduleservice.service.GymService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/gyms")
public class GymController {

    private final GymService gymService;
    private final GroupClassScheduleService groupClassScheduleService;

    public GymController(GymService gymService, GroupClassScheduleService groupClassScheduleService) {
        this.gymService = gymService;
        this.groupClassScheduleService = groupClassScheduleService;
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
    public ResponseEntity<List<GroupClassDefinitionDTO>> getGymGroupClassOfferings(@PathVariable Long id) {
        List<GroupClassDefinitionDTO> groupClassDefinitions = gymService.getGymGroupClassOfferings(id)
            .stream()
            .map(GymGroupClassOfferingDTO::groupClassDefinition)
            .toList();
        return ResponseEntity.ok(groupClassDefinitions);
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

    @GetMapping("/{gymId}/group-classes")
    public ResponseEntity<List<GroupClassScheduleDTO>> getGroupClassesByGym(@PathVariable Long gymId) {
        List<GroupClassScheduleDTO> groupClasses = groupClassScheduleService.getGroupClassesByGymId(gymId);
        return ResponseEntity.ok(groupClasses);
    }

}
