package org.zzpj.gymapp.scheduleservice.controller;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zzpj.gymapp.scheduleservice.dto.GroupClassScheduleDTO;
import org.zzpj.gymapp.scheduleservice.model.GroupClassSchedule;
import org.zzpj.gymapp.scheduleservice.service.GroupClassScheduleService;

import java.util.List;

@RestController
@RequestMapping("/schedule")
public class GroupClassScheduleController {

    private final GroupClassScheduleService groupClassScheduleService;

    public GroupClassScheduleController(GroupClassScheduleService groupClassScheduleService) {
        this.groupClassScheduleService = groupClassScheduleService;
    }

    @PostMapping("/add-group-class")
    public ResponseEntity<GroupClassSchedule> addGroupClassSchedule(@RequestBody GroupClassSchedule groupClassSchedule) {
        return ResponseEntity.ok(groupClassScheduleService.addGroupClassSchedule(groupClassSchedule));
    }

    @PostMapping("/{scheduleId}/signup/{userId}")
    public ResponseEntity<GroupClassSchedule> signUpUser(
            @PathVariable Long scheduleId,
            @PathVariable Long userId) {
        return ResponseEntity.ok(groupClassScheduleService.signUpUser(scheduleId, userId));
    }

    @GetMapping("/all-group-classes-dto")
    public ResponseEntity<List<GroupClassScheduleDTO>> getAllGroupClassSchedulesDTO() {
        return ResponseEntity.ok(groupClassScheduleService.getAllGroupClassScheduleDTOs());
    }

}
