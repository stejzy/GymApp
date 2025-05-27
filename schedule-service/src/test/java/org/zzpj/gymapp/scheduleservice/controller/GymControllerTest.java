package org.zzpj.gymapp.scheduleservice.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.zzpj.gymapp.scheduleservice.dto.GroupClassDefinitionDTO;
import org.zzpj.gymapp.scheduleservice.dto.GymDTO;
import org.zzpj.gymapp.scheduleservice.dto.GymGroupClassOfferingDTO;
import org.zzpj.gymapp.scheduleservice.service.GymService;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.time.LocalTime;

class GymControllerTest {

    private GymService gymService;
    private GymController gymController;

    @BeforeEach
    void setUp() {
        gymService = mock(GymService.class);
        gymController = new GymController(gymService);
    }

    @Test
    void getAllGyms_shouldReturnListOfGyms() {
        List<GymDTO> gyms = List.of(
                new GymDTO(1L, "Gym A", "CityA", "Address A", "123456789", LocalTime.of(6,0), LocalTime.of(22,0)),
                new GymDTO(2L, "Gym B", "CityB", "Address B", "987654321", LocalTime.of(7,0), LocalTime.of(21,0))
        );
        when(gymService.getAllGyms()).thenReturn(gyms);

        ResponseEntity<List<GymDTO>> response = gymController.getAllGyms();

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(gyms);
        verify(gymService, times(1)).getAllGyms();
    }

    @Test
    void getGymById_shouldReturnGym() {
        GymDTO gym = new GymDTO(1L, "Gym A", "CityA", "Address A", "123456789", LocalTime.of(6,0), LocalTime.of(22,0));
        when(gymService.getGymById(1L)).thenReturn(gym);

        ResponseEntity<GymDTO> response = gymController.getGymById(1L);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(gym);
        verify(gymService, times(1)).getGymById(1L);
    }

    @Test
    void getGymGroupClassOfferings_shouldReturnList() {
        GymDTO gymDTO = new GymDTO(1L, "Gym A", "CityA", "Address A", "123456789", LocalTime.of(6,0), LocalTime.of(22,0));
        GroupClassDefinitionDTO groupClassDefDTO = new GroupClassDefinitionDTO(10L, "Yoga", "Morning yoga class");

        List<GymGroupClassOfferingDTO> offerings = List.of(
                new GymGroupClassOfferingDTO(100L, gymDTO, groupClassDefDTO),
                new GymGroupClassOfferingDTO(101L, gymDTO, new GroupClassDefinitionDTO(11L, "CrossFit", "High intensity"))
        );

        when(gymService.getGymGroupClassOfferings(1L)).thenReturn(offerings);

        ResponseEntity<List<GymGroupClassOfferingDTO>> response = gymController.getGymGroupClassOfferings(1L);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(offerings);
        verify(gymService, times(1)).getGymGroupClassOfferings(1L);
    }
}
