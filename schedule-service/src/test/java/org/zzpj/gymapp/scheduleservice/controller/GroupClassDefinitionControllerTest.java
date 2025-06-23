package org.zzpj.gymapp.scheduleservice.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zzpj.gymapp.scheduleservice.dto.GroupClassDefinitionDTO;
import org.zzpj.gymapp.scheduleservice.service.GroupClassDefinitionService;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GroupClassDefinitionControllerTest {

    private GroupClassDefinitionService service;
    private GroupClassDefinitionController controller;

    @BeforeEach
    void setUp() {
        service = mock(GroupClassDefinitionService.class);
        controller = new GroupClassDefinitionController(service);
    }

    @Test
    void getGroupClassDefinitions_shouldReturnListFromService() {
        List<GroupClassDefinitionDTO> expectedList = List.of(
                new GroupClassDefinitionDTO(1L, "Yoga", "Relaxing yoga classes"),
                new GroupClassDefinitionDTO(2L, "Pilates", "Core strengthening exercises")
        );
        when(service.getAllGroupClassDefinitions()).thenReturn(expectedList);

        ResponseEntity<List<GroupClassDefinitionDTO>> response = controller.getGroupClassDefinitions();

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(expectedList);
        verify(service, times(1)).getAllGroupClassDefinitions();
    }

    @Test
    void getGroupClassDefinitionById_shouldReturnDtoFromService() {
        Long id = 1L;
        GroupClassDefinitionDTO dto = new GroupClassDefinitionDTO(id, "Yoga", "Relaxing yoga classes");
        when(service.getGroupClassDefinitionById(id)).thenReturn(dto);

        ResponseEntity<GroupClassDefinitionDTO> response = controller.getGroupClassDefinitionById(id);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(dto);
        verify(service, times(1)).getGroupClassDefinitionById(id);
    }
}
