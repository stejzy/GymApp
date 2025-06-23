package org.zzpj.gymapp.scheduleservice.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.zzpj.gymapp.scheduleservice.model.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
class GroupClassScheduleRepositoryTest {
    @Autowired
    private GroupClassScheduleRepository repository;
    @Autowired
    private GymGroupClassOfferingRepository offeringRepository;
    @Autowired
    private GymRepository gymRepository;
    @Autowired
    private GroupClassDefinitionRepository definitionRepository;

    @Test
    void findConflictingGroupSchedules_shouldReturnConflicts() {
        Gym gym = new Gym();
        gym.setName("TestGym");
        gym.setCity("TestCity");
        gym.setAddress("TestAddress");
        gym.setPhoneNumber("123456789");
        gym.setOpeningHour(LocalTime.of(8, 0));
        gym.setClosingHour(LocalTime.of(22, 0));
        gym.setTrainerIds(List.of(1L));
        gym = gymRepository.save(gym);

        GroupClassDefinition def = new GroupClassDefinition();
        def.setName("Yoga");
        def.setDescription("desc");
        def = definitionRepository.save(def);

        GymGroupClassOffering offering = new GymGroupClassOffering();
        offering.setGym(gym);
        offering.setGroupClassDefinition(def);
        offering = offeringRepository.save(offering);

        GroupClassSchedule schedule = new GroupClassSchedule();
        schedule.setGymGroupClassOffering(offering);
        schedule.setTrainerId(1L);
        schedule.setStartTime(LocalDateTime.of(2024, 6, 1, 10, 0));
        schedule.setEndTime(LocalDateTime.of(2024, 6, 1, 11, 0));
        schedule.setCapacity(10);
        repository.save(schedule);

        List<GroupClassSchedule> conflicts = repository.findConflictingGroupSchedules(
                gym.getId(),
                LocalDateTime.of(2024, 6, 1, 10, 30),
                LocalDateTime.of(2024, 6, 1, 11, 30)
        );
        assertThat(conflicts).isNotEmpty();
    }
} 