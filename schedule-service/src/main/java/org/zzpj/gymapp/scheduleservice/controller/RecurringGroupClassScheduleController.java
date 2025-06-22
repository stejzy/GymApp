package org.zzpj.gymapp.scheduleservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zzpj.gymapp.scheduleservice.dto.RequestRecurringGroupClassScheduleDTO;
import org.zzpj.gymapp.scheduleservice.dto.ResponseRecurringGroupClassScheduleDTO;
import org.zzpj.gymapp.scheduleservice.service.RecurringGroupClassScheduleService;

import java.util.List;

@RestController
@RequestMapping("/schedule")
public class RecurringGroupClassScheduleController {

    private final RecurringGroupClassScheduleService recurringGroupClassScheduleService;

    public RecurringGroupClassScheduleController(RecurringGroupClassScheduleService recurringGroupClassScheduleService) {
        this.recurringGroupClassScheduleService = recurringGroupClassScheduleService;
    }

    @PostMapping("/add-recurring-group-class")
    public ResponseEntity<ResponseRecurringGroupClassScheduleDTO> addRecurringGroupClassSchedule(@RequestBody RequestRecurringGroupClassScheduleDTO newGroupClasses) {
        return ResponseEntity.ok(recurringGroupClassScheduleService.addRecurringGroupClassSchedule(newGroupClasses));
    }

    @GetMapping("/get-recurring-group-classes/{id}")
    public ResponseEntity<List<ResponseRecurringGroupClassScheduleDTO>> getRecurringGroupClassSchedule(@PathVariable Long id) {
        return ResponseEntity.ok(recurringGroupClassScheduleService.getAllRecurringGroupClassByGymId(id));
    }

    @GetMapping("/get-all-recurring-group-classes")
    public ResponseEntity<List<ResponseRecurringGroupClassScheduleDTO>> getAllRecurringGroupClass() {
        return ResponseEntity.ok(recurringGroupClassScheduleService.getAllRecurringGroupClass());
    }
}
