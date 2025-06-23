package org.zzpj.gymapp.scheduleservice.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class GroupClassScheduleTest {
    @Test
    void testSettersAndGetters() {
        GroupClassSchedule schedule = new GroupClassSchedule();
        schedule.setId(1L);
        schedule.setTrainerId(2L);
        schedule.setStartTime(LocalDateTime.of(2024, 6, 1, 10, 0));
        schedule.setEndTime(LocalDateTime.of(2024, 6, 1, 11, 0));
        schedule.setCapacity(10);
        schedule.setParticipantIds(List.of(3L, 4L));

        assertThat(schedule.getId()).isEqualTo(1L);
        assertThat(schedule.getTrainerId()).isEqualTo(2L);
        assertThat(schedule.getStartTime()).isEqualTo(LocalDateTime.of(2024, 6, 1, 10, 0));
        assertThat(schedule.getEndTime()).isEqualTo(LocalDateTime.of(2024, 6, 1, 11, 0));
        assertThat(schedule.getCapacity()).isEqualTo(10);
        assertThat(schedule.getParticipantIds()).containsExactly(3L, 4L);
    }
} 