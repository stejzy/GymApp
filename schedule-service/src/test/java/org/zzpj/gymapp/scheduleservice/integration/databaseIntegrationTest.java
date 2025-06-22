package org.zzpj.gymapp.scheduleservice.integration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
//import org.springframework.web.client.RestTemplate;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.web.client.RestTemplate;
import org.zzpj.gymapp.scheduleservice.dto.*;
import org.zzpj.gymapp.scheduleservice.model.*;
import org.zzpj.gymapp.scheduleservice.repository.GroupClassDefinitionRepository;
import org.zzpj.gymapp.scheduleservice.repository.GymGroupClassOfferingRepository;
import org.zzpj.gymapp.scheduleservice.repository.GymRepository;
import org.zzpj.gymapp.scheduleservice.repository.RecurringGroupClassScheduleRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class databaseIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private RecurringGroupClassScheduleRepository recurringGroupClassScheduleRepository;

    @Autowired
    private GymGroupClassOfferingRepository gymGroupClassOfferingRepository;

    @Autowired
    private GymRepository gymRepository;

    @Autowired
    private GroupClassDefinitionRepository groupClassDefinitionRepository;

    private String baseUrl="";

    private static RestTemplate restTemplate;

    @BeforeAll
    public void init(){
        restTemplate = new RestTemplate();

        Gym gym1 = new Gym( //id = 1
                null,
                "Wyciskara",
                "Lodz",
                "pomarańczowa 12",
                "123456789",
                LocalTime.of(8, 0),
                LocalTime.of(21, 0),
                List.of(),
                List.of()
        );

        Gym gym2 = new Gym( //id = 2
                null,
                "Mordownia",
                "Lodz",
                "piwnica 12",
                "123456789",
                LocalTime.of(8, 0),
                LocalTime.of(21, 0),
                List.of(),
                List.of()
        );
        gym1 = gymRepository.save(gym1);
        gym2 = gymRepository.save(gym2);


        GroupClassDefinition groupClass = new GroupClassDefinition( //id = 1
                null,
                "Trening wielkiego chłopa",
                "Wyciskanie na mordowni"
        );
        groupClass = groupClassDefinitionRepository.save(groupClass);


        GymGroupClassOffering offering1 = new GymGroupClassOffering( //id = 1
                null,
                gym1,
                groupClass,
                List.of(),
                List.of()
        );

        GymGroupClassOffering offering2 = new GymGroupClassOffering( //id = 1
                null,
                gym2,
                groupClass,
                List.of(),
                List.of()
        );
        offering1 = gymGroupClassOfferingRepository.save(offering1);
        offering2 = gymGroupClassOfferingRepository.save(offering2);

        RecurringGroupClassSchedule class1 = new RecurringGroupClassSchedule( //id = 1
                null,
                offering1,
                null,
                2L,
                DayOfWeek.MONDAY,
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                LocalDate.of(2024, 6, 3),
                LocalDate.of(2024, 6, 24),
                Frequency.WEEKLY,
                12
        );

        RecurringGroupClassSchedule class2 = new RecurringGroupClassSchedule( //id = 2
                null,
                offering2,
                null,
                2L,
                DayOfWeek.MONDAY,
                LocalTime.of(14, 0),
                LocalTime.of(16, 0),
                LocalDate.of(2024, 6, 3),
                LocalDate.of(2024, 6, 24),
                Frequency.WEEKLY,
                40
        );

        recurringGroupClassScheduleRepository.save(class1);
        recurringGroupClassScheduleRepository.save(class2);
    }

    @BeforeEach
    public void setUp(){
        baseUrl="http://localhost:"+port+"/schedule";
    }

    ///add-recurring-group-class
    @Test
    @Sql(statements = "DELETE FROM recurring_group_class_schedule WHERE recurring_group_class_schedule_id='3'",executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void addRecurringGroupClassScheduleTest(){

        RequestRecurringGroupClassScheduleDTO requestDto = new RequestRecurringGroupClassScheduleDTO(
                1L,
                2L,
                DayOfWeek.MONDAY,
                LocalTime.of(8, 0),
                LocalTime.of(9, 0),
                LocalDate.of(2026, 6, 3),
                LocalDate.of(2026, 6, 24),
                Frequency.WEEKLY,
                12
        );

        ResponseRecurringGroupClassScheduleDTO response = restTemplate.postForObject(
                baseUrl + "/add-recurring-group-class",
                requestDto,
                ResponseRecurringGroupClassScheduleDTO.class);

        assertEquals(3L, response.id());
        assertEquals(requestDto.capacity(), response.capacity());
    }

    ///get-all-recurring-group-classes
    @Test
    public void getAllRecurringGroupClassesTest(){
        List<RecurringGroupClassSchedule> response = restTemplate.getForObject(baseUrl
                + "/get-all-recurring-group-classes", List.class);
        assertEquals(2, recurringGroupClassScheduleRepository.findAll().size());
        assertEquals(response.size(), recurringGroupClassScheduleRepository.findAll().size());
    }

    ///get-recurring-group-classes/{id}
    @Test
    public void getRecurringGroupClassesByIdTest(){
        List<RecurringGroupClassSchedule> response = restTemplate.getForObject(baseUrl
                +"/get-recurring-group-classes/{id}", List.class, 1L);

        assertEquals(2, recurringGroupClassScheduleRepository.findAll().size()); //Equals expected 2 i w repo są 2
        assertEquals(1, response.size()); //Equals expected 1 i pobralo po id gym tez 1
        assertNotEquals(response.size(), recurringGroupClassScheduleRepository.findAll().size()); //NotEuals expected 1 a w repo są 2
    }

}