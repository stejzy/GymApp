package org.zzpj.gymapp.scheduleservice.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.zzpj.gymapp.scheduleservice.model.GroupClassSchedule;
import org.zzpj.gymapp.scheduleservice.service.GroupClassScheduleService;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import org.zzpj.gymapp.scheduleservice.model.GymGroupClassOffering;
import java.time.LocalDateTime;




class GroupClassScheduleControllerTest {

    private GroupClassScheduleService service;
    private GroupClassScheduleController controller;

    @BeforeEach
    void setUp() {
        service = mock(GroupClassScheduleService.class);
        controller = new GroupClassScheduleController(service);
    }

    @Test
    void addGroupClassSchedule_shouldCallServiceAndReturnResult() {
        GymGroupClassOffering offering = new GymGroupClassOffering();
        offering.setId(1L);

        GroupClassSchedule inputSchedule = new GroupClassSchedule();
        inputSchedule.setId(null);
        inputSchedule.setGymGroupClassOffering(offering);
        inputSchedule.setTrainerId(42L);
        inputSchedule.setStartTime(LocalDateTime.of(2025, 6, 1, 9, 0));
        inputSchedule.setEndTime(LocalDateTime.of(2025, 6, 1, 10, 0));
        inputSchedule.setCapacity(15);
        inputSchedule.getParticipantIds().add(1001L);
        inputSchedule.getParticipantIds().add(1002L);

        GroupClassSchedule savedSchedule = new GroupClassSchedule();
        savedSchedule.setId(10L);
        savedSchedule.setGymGroupClassOffering(offering);
        savedSchedule.setTrainerId(42L);
        savedSchedule.setStartTime(LocalDateTime.of(2025, 6, 1, 9, 0));
        savedSchedule.setEndTime(LocalDateTime.of(2025, 6, 1, 10, 0));
        savedSchedule.setCapacity(15);
        savedSchedule.getParticipantIds().add(1001L);
        savedSchedule.getParticipantIds().add(1002L);

        when(service.addGroupClassSchedule(inputSchedule)).thenReturn(savedSchedule);

        ResponseEntity<GroupClassSchedule> response = controller.addGroupClassSchedule(inputSchedule);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(savedSchedule);

        ArgumentCaptor<GroupClassSchedule> captor = ArgumentCaptor.forClass(GroupClassSchedule.class);
        verify(service, times(1)).addGroupClassSchedule(captor.capture());
        assertThat(captor.getValue()).isEqualTo(inputSchedule);
    }

}
