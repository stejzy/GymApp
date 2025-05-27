package org.zzpj.gymapp.scheduleservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zzpj.gymapp.scheduleservice.dto.GroupClassDefinitionDTO;
import org.zzpj.gymapp.scheduleservice.service.GroupClassDefinitionService;

import java.util.List;

@RestController
@RequestMapping("/schedule/group-classes")
public class GroupClassDefinitionController {

    private final GroupClassDefinitionService groupClassDefinitionService;

    public GroupClassDefinitionController(GroupClassDefinitionService groupClassDefinitionService) {
        this.groupClassDefinitionService = groupClassDefinitionService;
    }

    @GetMapping("")
    public ResponseEntity<List<GroupClassDefinitionDTO>> getGroupClassDefinitions() {
        return ResponseEntity.ok(groupClassDefinitionService.getAllGroupClassDefinitions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupClassDefinitionDTO> getGroupClassDefinitionById(@PathVariable Long id) {
        return ResponseEntity.ok(groupClassDefinitionService.getGroupClassDefinitionById(id));
    }
}
