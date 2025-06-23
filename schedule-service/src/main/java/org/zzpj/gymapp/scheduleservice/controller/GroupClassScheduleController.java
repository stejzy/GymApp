package org.zzpj.gymapp.scheduleservice.controller;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zzpj.gymapp.scheduleservice.dto.CreateGroupClassScheduleDTO;
import org.zzpj.gymapp.scheduleservice.dto.GroupClassScheduleDTO;
import org.zzpj.gymapp.scheduleservice.dto.UserProfileResponseDTO;
import org.zzpj.gymapp.scheduleservice.model.GroupClassSchedule;
import org.zzpj.gymapp.scheduleservice.service.GroupClassScheduleService;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/singleGroupClass")
public class GroupClassScheduleController {

    private final GroupClassScheduleService groupClassScheduleService;

    public GroupClassScheduleController(GroupClassScheduleService groupClassScheduleService) {
        this.groupClassScheduleService = groupClassScheduleService;
    }

    @PostMapping("/add-group-class")
    public ResponseEntity<GroupClassScheduleDTO > addGroupClassSchedule(
            @RequestParam Long gymGroupClassOfferingId,
            @RequestParam Long trainerId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime endTime,
            @RequestParam Integer capacity) {

        CreateGroupClassScheduleDTO dto = new CreateGroupClassScheduleDTO(
                gymGroupClassOfferingId,
                trainerId,
                startTime,
                endTime,
                capacity
        );

        GroupClassScheduleDTO  created = groupClassScheduleService.addGroupClassSchedule(dto);
        return ResponseEntity.ok(created);
    }

    @PostMapping("/{scheduleId}/signup")
    public ResponseEntity<GroupClassScheduleDTO> signUpUser(
            @PathVariable Long scheduleId,
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(groupClassScheduleService.signUpUser(scheduleId, userId));
    }

    @GetMapping("/all-group-classes-dto")
    public ResponseEntity<List<GroupClassScheduleDTO>> getAllGroupClassSchedulesDTO() {
        return ResponseEntity.ok(groupClassScheduleService.getAllGroupClassScheduleDTOs());
    }

    @GetMapping("/{scheduleId}/participants")
    public Mono<ResponseEntity<List<UserProfileResponseDTO>>> getParticipants(
            @PathVariable Long scheduleId,
            @RequestHeader("Authorization") String authHeader) {

        return groupClassScheduleService.getParticipantsProfiles(scheduleId, authHeader)
                .map(ResponseEntity::ok);
    }

}
