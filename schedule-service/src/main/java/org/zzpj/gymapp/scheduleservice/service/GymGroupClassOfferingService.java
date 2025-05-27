package org.zzpj.gymapp.scheduleservice.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.zzpj.gymapp.scheduleservice.dto.GroupClassDefinitionDTO;
import org.zzpj.gymapp.scheduleservice.dto.GymDTO;
import org.zzpj.gymapp.scheduleservice.dto.GymGroupClassOfferingDTO;
import org.zzpj.gymapp.scheduleservice.model.GymGroupClassOffering;
import org.zzpj.gymapp.scheduleservice.repository.GymGroupClassOfferingRepository;

@Service
public class GymGroupClassOfferingService {
    private final GymGroupClassOfferingRepository gymGroupClassOfferingRepository;
    private final GroupClassDefinitionService groupClassDefinitionService;
    private final GymService gymService;

    public GymGroupClassOfferingService(GroupClassDefinitionService groupClassDefinitionService,
                                        @Lazy GymService gymService,
                                        GymGroupClassOfferingRepository gymGroupClassOfferingRepository
    ) {
        this.groupClassDefinitionService = groupClassDefinitionService;
        this.gymService = gymService;
        this.gymGroupClassOfferingRepository = gymGroupClassOfferingRepository;
    }

    public GymGroupClassOfferingDTO getGymGroupClassOfferingById(Long id) {

        GymGroupClassOffering gymGroupClassOffering = gymGroupClassOfferingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("GymGroupClassOffering not found with id: " + id));

        return mapToDto(gymGroupClassOffering);
    }

    public GymGroupClassOfferingDTO mapToDto(GymGroupClassOffering gymGroupClassOffering) {

        GymDTO gymDTO = gymService.getGymById(gymGroupClassOffering.getGym().getId());
        GroupClassDefinitionDTO groupClassDefinitionDTO = groupClassDefinitionService.getGroupClassDefinitionById(
                gymGroupClassOffering.getGroupClassDefinition().getId());

        return new GymGroupClassOfferingDTO(
                gymGroupClassOffering.getId(),
                gymDTO,
                groupClassDefinitionDTO
        );
    }
}
