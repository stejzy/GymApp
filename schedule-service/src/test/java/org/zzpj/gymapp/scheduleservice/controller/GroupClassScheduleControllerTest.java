package org.zzpj.gymapp.scheduleservice.controller;

import org.junit.jupiter.api.Test;
import org.zzpj.gymapp.scheduleservice.model.GroupClassSchedule;

import org.zzpj.gymapp.scheduleservice.model.GymGroupClassOffering;
import java.time.LocalDateTime;




class GroupClassScheduleControllerTest {


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
    }

}
