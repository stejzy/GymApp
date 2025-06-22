package org.zzpj.gymapp.scheduleservice.cucumber;

import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.zzpj.gymapp.scheduleservice.ScheduleServiceApplication;
import org.zzpj.gymapp.scheduleservice.dto.GymDTO;
import org.zzpj.gymapp.scheduleservice.dto.RequestRecurringGroupClassScheduleDTO;
import org.zzpj.gymapp.scheduleservice.dto.ResponseRecurringGroupClassScheduleDTO;
import org.zzpj.gymapp.scheduleservice.model.GroupClassDefinition;
import org.zzpj.gymapp.scheduleservice.model.Gym;
import org.zzpj.gymapp.scheduleservice.model.GymGroupClassOffering;
import org.zzpj.gymapp.scheduleservice.repository.GroupClassDefinitionRepository;
import org.zzpj.gymapp.scheduleservice.repository.GymGroupClassOfferingRepository;
import org.zzpj.gymapp.scheduleservice.repository.GymRepository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@ActiveProfiles("test")
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class RecurringGroupClassScheduleSteps {

    @LocalServerPort
    private int port;


    private final StepDefinitionsContext context;

    @Autowired
    private GymGroupClassOfferingRepository gymGroupClassOfferingRepository;

    @Autowired
    private GroupClassDefinitionRepository groupClassDefinitionRepository;

    @Autowired
    private GymRepository gymRepository;


    private ResponseEntity<ResponseRecurringGroupClassScheduleDTO> response;

    public RecurringGroupClassScheduleSteps(StepDefinitionsContext context) {
        this.context = context;
    }

    @Before
    public void beforeScenario() {
        context.baseUrl = "http://localhost:" + port + "/schedule";
    }


    @Given("a gym group class offering with id 1 exists")
    public void givenGymGroupClassOfferingExists() {
        // Tworzenie i zapis definicji grupowych zajęć
        GroupClassDefinition definition = new GroupClassDefinition();
        definition.setName("Test group class");
        definition.setDescription("Opis testowych zajęć");
        groupClassDefinitionRepository.save(definition);

        // Tworzenie i zapis przykładowej siłowni
        Gym gym = new Gym();
        gym.setName("Test Gym");
        gym.setAddress("Testowa 1, Wrocław");
        gymRepository.save(gym);

        // Tworzenie i zapis oferty zajęć powiązanej z siłownią i definicją
        GymGroupClassOffering offering = new GymGroupClassOffering();
        offering.setGroupClassDefinition(definition);
        offering.setGym(gym);
        gymGroupClassOfferingRepository.save(offering);

        // Jeśli chcesz zapamiętać ofertę do dalszego użycia (np. do StepDefinitionsContext), zrób to tutaj
    }




    @When("I add a recurring group class schedule with the following data:")
    public void whenIAddARecurringGroupClassScheduleWithTheFollowingData(io.cucumber.datatable.DataTable dataTable) {
        Map<String, String> data = dataTable.asMaps().get(0);

        LocalDate startDate = parseDateOrPlaceholder(data.get("startDate"));
        LocalDate endDate = parseDateOrPlaceholder(data.get("endDate"));


        RequestRecurringGroupClassScheduleDTO request = new RequestRecurringGroupClassScheduleDTO(
                Long.parseLong(data.get("gymGroupClassOfferingId")),
                Long.parseLong(data.get("trainerId")),
                DayOfWeek.valueOf(data.get("dayOfWeek")),
                LocalTime.parse(data.get("startTime")),
                LocalTime.parse(data.get("endTime")),
                startDate,
                endDate,
                Enum.valueOf(org.zzpj.gymapp.scheduleservice.model.Frequency.class, data.get("frequency")),
                Integer.parseInt(data.get("capacity"))
        );

        response = context.restTemplate.postForEntity(context.baseUrl + "/add-recurring-group-class", request, ResponseRecurringGroupClassScheduleDTO.class);
    }

    private LocalDate parseDateOrPlaceholder(String value) {
        if (value == null) return null;
        if (value.startsWith("TODAY+")) {
            String days = value.substring("TODAY+".length(), value.indexOf("DAYS"));
            return LocalDate.now().plusDays(Long.parseLong(days));
        } else if ("TODAY".equals(value)) {
            return LocalDate.now();
        } else {
            return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
        }
    }


    @Then("the recurring group class schedule is saved successfully")
    public void thenTheRecurringGroupClassScheduleShouldBeCreatedSuccessfully() {
        assertThat(response).isNotNull();
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isNotNull();
    }

    @And("the returned schedule contains the gymGroupClassOfferingId {long}")
    public void assertGymGroupClassOfferingId(Long id) {
        assertThat(response).isNotNull();
        assertThat(response.getBody().gymGroupClassOffering().id()).isEqualTo(id);
    }

    @And("the returned schedule contains the trainerId {long}")
    public void assertTrainerId(Long id) {
        assertThat(response.getBody().trainerId()).isEqualTo(id);
    }

    @And("the returned schedule contains dayOfWeek {word}")
    public void assertDayOfWeek(String day) {
        assertThat(response.getBody().dayOfWeek()).isEqualTo(DayOfWeek.valueOf(day));
    }


}