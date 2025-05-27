package org.zzpj.gymapp.scheduleservice.controller;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zzpj.gymapp.scheduleservice.model.GroupClassSchedule;
import org.zzpj.gymapp.scheduleservice.service.GroupClassScheduleService;

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

}
