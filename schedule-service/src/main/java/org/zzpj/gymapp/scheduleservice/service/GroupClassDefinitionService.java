package org.zzpj.gymapp.scheduleservice.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.zzpj.gymapp.scheduleservice.dto.GroupClassDefinitionDTO;
import org.zzpj.gymapp.scheduleservice.exeption.GroupClassDefinitionException;
import org.zzpj.gymapp.scheduleservice.model.GroupClassDefinition;
import org.zzpj.gymapp.scheduleservice.repository.GroupClassDefinitionRepository;

import java.util.List;

@Service
public class GroupClassDefinitionService {
    private final GroupClassDefinitionRepository groupClassDefinitionRepository;

    public GroupClassDefinitionService(GroupClassDefinitionRepository groupClassDefinitionRepository) {
        this.groupClassDefinitionRepository = groupClassDefinitionRepository;
    }

    public List<GroupClassDefinitionDTO> getAllGroupClassDefinitions() {
        return groupClassDefinitionRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public GroupClassDefinitionDTO getGroupClassDefinitionById(Long id) {
        return groupClassDefinitionRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new EntityNotFoundException("GroupClassDefinition with ID " + id + " not found"));
    }

    private GroupClassDefinitionDTO mapToDTO(GroupClassDefinition groupClassDefinition) {
        return new GroupClassDefinitionDTO(
                groupClassDefinition.getId(),
                groupClassDefinition.getName(),
                groupClassDefinition.getDescription()
        );
    }
}
