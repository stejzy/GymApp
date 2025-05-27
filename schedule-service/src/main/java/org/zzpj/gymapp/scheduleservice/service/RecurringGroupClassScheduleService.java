package org.zzpj.gymapp.scheduleservice.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.zzpj.gymapp.scheduleservice.dto.RequestRecurringGroupClassScheduleDTO;
import org.zzpj.gymapp.scheduleservice.dto.ResponseRecurringGroupClassScheduleDTO;
import org.zzpj.gymapp.scheduleservice.exeption.ScheduleConflictException;
import org.zzpj.gymapp.scheduleservice.model.GroupClassSchedule;
import org.zzpj.gymapp.scheduleservice.model.GymGroupClassOffering;
import org.zzpj.gymapp.scheduleservice.model.RecurringGroupClassSchedule;
import org.zzpj.gymapp.scheduleservice.repository.GroupClassScheduleRepository;
import org.zzpj.gymapp.scheduleservice.repository.GymGroupClassOfferingRepository;
import org.zzpj.gymapp.scheduleservice.repository.RecurringGroupClassScheduleRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class RecurringGroupClassScheduleService {
    private final RecurringGroupClassScheduleRepository recurringGroupClassScheduleRepository;
    private final GymGroupClassOfferingRepository gymGroupClassOfferingRepository;
    private final GymGroupClassOfferingService gymGroupClassOfferingService;
    private final GroupClassScheduleRepository groupClassScheduleRepository;
    private final GroupClassScheduleGenerator groupClassScheduleGenerator;

    public RecurringGroupClassScheduleService(RecurringGroupClassScheduleRepository recurringGroupClassScheduleRepository,
                                              GymGroupClassOfferingService gymGroupClassOfferingService,
                                              GymGroupClassOfferingRepository gymGroupClassOfferingRepository,
                                              GroupClassScheduleRepository groupClassScheduleRepository,
                                              GroupClassScheduleGenerator groupClassScheduleGenerator) {
        this.recurringGroupClassScheduleRepository = recurringGroupClassScheduleRepository;
        this.gymGroupClassOfferingService = gymGroupClassOfferingService;
        this.gymGroupClassOfferingRepository = gymGroupClassOfferingRepository;
        this.groupClassScheduleRepository = groupClassScheduleRepository;
        this.groupClassScheduleGenerator = groupClassScheduleGenerator;
    }

    public ResponseRecurringGroupClassScheduleDTO addRecurringGroupClassSchedule(RequestRecurringGroupClassScheduleDTO dto) {

        RecurringGroupClassSchedule newGroupClasses = mapDtoToEntity(dto);

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        if (newGroupClasses.getStartDate().isBefore(tomorrow)) {
            throw new IllegalArgumentException("Start date must be at least tomorrow or later.");
        }

        System.out.println("Zmapowało");

        Long gymId = newGroupClasses.getGymGroupClassOffering().getGym().getId();
        DayOfWeek dayOfWeek = newGroupClasses.getDayOfWeek();
        LocalTime startTime = newGroupClasses.getStartTime();
        LocalTime endTime = newGroupClasses.getEndTime();

        LocalDate currentDate = newGroupClasses.getStartDate();

        while(!currentDate.isAfter(newGroupClasses.getEndDate())){

            if (currentDate.getDayOfWeek() == dayOfWeek) {
                List<RecurringGroupClassSchedule> conflicts = recurringGroupClassScheduleRepository.
                        findConflictingSchedulesOnDate(
                        gymId, dayOfWeek, currentDate, startTime, endTime);

                if(!conflicts.isEmpty()) {
                    throw new ScheduleConflictException("Conflict with existing schedule on " + currentDate);
                }

                // Konflikt z pojedynczymi zajęciami
                LocalDateTime fullStart = LocalDateTime.of(currentDate, startTime);
                LocalDateTime fullEnd = LocalDateTime.of(currentDate, endTime);

                List<GroupClassSchedule> singleConflicts = groupClassScheduleRepository
                        .findConflictingGroupSchedules(gymId, fullStart, fullEnd);

                if (!singleConflicts.isEmpty()) {
                    throw new ScheduleConflictException("Conflict with existing single schedule on " + currentDate);
                }
            }

            currentDate = currentDate.plusDays(1);
        }

        recurringGroupClassScheduleRepository.save(newGroupClasses);

        LocalDate now = LocalDate.now().plusDays(1); // od jutra
        LocalDate oneMonthAhead = now.plusMonths(1);
        groupClassScheduleGenerator.generateForRecurringSchedule(newGroupClasses, now, oneMonthAhead);

        return mapEntityToDto(newGroupClasses);
    }

    public List<ResponseRecurringGroupClassScheduleDTO> getAllRecurringGroupClassByGymId(Long gymId) {
        return recurringGroupClassScheduleRepository.findAllByGymGroupClassOffering_Gym_Id(gymId)
                .stream()
                .map(this::mapEntityToDto)
                .toList();
    }

    public List<ResponseRecurringGroupClassScheduleDTO> getAllRecurringGroupClass(){
        return recurringGroupClassScheduleRepository.findAll()
                .stream()
                .map(this::mapEntityToDto)
                .toList();
    }

    private RecurringGroupClassSchedule mapDtoToEntity(RequestRecurringGroupClassScheduleDTO dto) {
        System.out.println(dto.capacity());
        System.out.println(dto.gymGroupClassOfferingId());
        GymGroupClassOffering offering = gymGroupClassOfferingRepository
                .findById(dto.gymGroupClassOfferingId())
                .orElseThrow(() -> new EntityNotFoundException("Offering not found"));

        return new RecurringGroupClassSchedule(
                null,
                offering,
                null,
                dto.trainerId(),
                dto.dayOfWeek(),
                dto.startTime(),
                dto.endTime(),
                dto.startDate(),
                dto.endDate(),
                dto.frequency(),
                dto.capacity()
        );
    }

    public ResponseRecurringGroupClassScheduleDTO mapEntityToDto(RecurringGroupClassSchedule entity) {
        return new ResponseRecurringGroupClassScheduleDTO(
                entity.getId(),
                gymGroupClassOfferingService.mapToDto(entity.getGymGroupClassOffering()),
                entity.getTrainerId(),
                entity.getDayOfWeek(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getFrequency(),
                entity.getCapacity()
        );
    }
}
