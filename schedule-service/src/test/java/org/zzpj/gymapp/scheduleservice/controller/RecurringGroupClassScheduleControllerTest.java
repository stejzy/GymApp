package org.zzpj.gymapp.scheduleservice.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.zzpj.gymapp.scheduleservice.dto.RequestRecurringGroupClassScheduleDTO;
import org.zzpj.gymapp.scheduleservice.dto.ResponseRecurringGroupClassScheduleDTO;
import org.zzpj.gymapp.scheduleservice.model.Frequency;
import org.zzpj.gymapp.scheduleservice.service.RecurringGroupClassScheduleService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RecurringGroupClassScheduleControllerTest {

    private RecurringGroupClassScheduleService service;
    private RecurringGroupClassScheduleController controller;

    @BeforeEach
    void setUp() {
        service = mock(RecurringGroupClassScheduleService.class);
        controller = new RecurringGroupClassScheduleController(service);
    }

    @Test
    void addRecurringGroupClassSchedule_shouldReturnCreatedSchedule() {
        RequestRecurringGroupClassScheduleDTO requestDTO = new RequestRecurringGroupClassScheduleDTO(
                1L, 2L, DayOfWeek.MONDAY, LocalTime.of(10,0), LocalTime.of(11,0),
                LocalDate.of(2025, 6, 1), LocalDate.of(2025, 8, 31), Frequency.WEEKLY, 20
        );
        ResponseRecurringGroupClassScheduleDTO responseDTO = new ResponseRecurringGroupClassScheduleDTO(
                100L, null, 2L, DayOfWeek.MONDAY, LocalTime.of(10,0), LocalTime.of(11,0),
                LocalDate.of(2025, 6, 1), LocalDate.of(2025, 8, 31), Frequency.WEEKLY, 20
        );
        when(service.addRecurringGroupClassSchedule(requestDTO)).thenReturn(responseDTO);

        ResponseEntity<ResponseRecurringGroupClassScheduleDTO> response = controller.addRecurringGroupClassSchedule(requestDTO);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(responseDTO);
        verify(service, times(1)).addRecurringGroupClassSchedule(requestDTO);
    }

    @Test
    void getRecurringGroupClassSchedule_shouldReturnList() {
        ResponseRecurringGroupClassScheduleDTO dto1 = new ResponseRecurringGroupClassScheduleDTO(
                100L, null, 2L, DayOfWeek.MONDAY, LocalTime.of(10,0), LocalTime.of(11,0),
                LocalDate.of(2025, 6, 1), LocalDate.of(2025, 8, 31), Frequency.WEEKLY, 20
        );
        ResponseRecurringGroupClassScheduleDTO dto2 = new ResponseRecurringGroupClassScheduleDTO(
                101L, null, 3L, DayOfWeek.WEDNESDAY, LocalTime.of(12,0), LocalTime.of(13,0),
                LocalDate.of(2025, 6, 3), LocalDate.of(2025, 8, 29), Frequency.WEEKLY, 15
        );
        List<ResponseRecurringGroupClassScheduleDTO> list = List.of(dto1, dto2);
        when(service.getAllRecurringGroupClassByGymId(1L)).thenReturn(list);

        ResponseEntity<List<ResponseRecurringGroupClassScheduleDTO>> response = controller.getRecurringGroupClassSchedule(1L);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(list);
        verify(service, times(1)).getAllRecurringGroupClassByGymId(1L);
    }

    @Test
    void getAllRecurringGroupClass_shouldReturnList() {
        List<ResponseRecurringGroupClassScheduleDTO> list = List.of(
                new ResponseRecurringGroupClassScheduleDTO(
                        100L, null, 2L, DayOfWeek.MONDAY, LocalTime.of(10,0), LocalTime.of(11,0),
                        LocalDate.of(2025, 6, 1), LocalDate.of(2025, 8, 31), Frequency.WEEKLY, 20
                )
        );
        when(service.getAllRecurringGroupClass()).thenReturn(list);

        ResponseEntity<List<ResponseRecurringGroupClassScheduleDTO>> response = controller.getAllRecurringGroupClass();

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(list);
        verify(service, times(1)).getAllRecurringGroupClass();
    }
}
