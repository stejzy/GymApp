package org.zzpj.gymapp.scheduleservice.controller;

import jakarta.ws.rs.Path;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zzpj.gymapp.scheduleservice.dto.GymDTO;
import org.zzpj.gymapp.scheduleservice.dto.GymGroupClassOfferingDTO;
import org.zzpj.gymapp.scheduleservice.model.Gym;
import org.zzpj.gymapp.scheduleservice.service.GymService;

import java.util.List;

@RestController
@RequestMapping("/schedule")
public class GymController {

    private final GymService gymService;

    public GymController(GymService gymService) {
        this.gymService = gymService;
    }

    @GetMapping("/gyms")
    public ResponseEntity<List<GymDTO>> getAllGyms() {
        return ResponseEntity.ok(gymService.getAllGyms());
    }

    @GetMapping("/gyms/{id}")
    public ResponseEntity<GymDTO> getGymById(@PathVariable Long id) {
        return ResponseEntity.ok(gymService.getGymById(id));
    }

    @GetMapping("/gyms/{id}/group-class-offerings")
    public ResponseEntity<List<GymGroupClassOfferingDTO>> getGymGroupClassOfferings(@PathVariable Long id) {
        return ResponseEntity.ok(gymService.getGymGroupClassOfferings(id));
    }
}
