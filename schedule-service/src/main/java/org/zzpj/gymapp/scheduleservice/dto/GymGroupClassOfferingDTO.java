package org.zzpj.gymapp.scheduleservice.dto;

public record GymGroupClassOfferingDTO(
        Long id,
        GymDTO gym,
        GroupClassDefinitionDTO groupClassDefinition
) {
}
