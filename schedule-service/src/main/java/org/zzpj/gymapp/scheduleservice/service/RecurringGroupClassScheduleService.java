package org.zzpj.gymapp.scheduleservice.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.zzpj.gymapp.scheduleservice.dto.RequestRecurringGroupClassScheduleDTO;
import org.zzpj.gymapp.scheduleservice.dto.ResponseRecurringGroupClassScheduleDTO;
import org.zzpj.gymapp.scheduleservice.exeption.ScheduleConflictException;
import org.zzpj.gymapp.scheduleservice.model.GymGroupClassOffering;
import org.zzpj.gymapp.scheduleservice.model.RecurringGroupClassSchedule;
import org.zzpj.gymapp.scheduleservice.repository.GymGroupClassOfferingRepository;
import org.zzpj.gymapp.scheduleservice.repository.RecurringGroupClassScheduleRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class RecurringGroupClassScheduleService {
    private final RecurringGroupClassScheduleRepository recurringGroupClassScheduleRepository;
    private final GymGroupClassOfferingRepository gymGroupClassOfferingRepository;
    private final GymGroupClassOfferingService gymGroupClassOfferingService;

    public RecurringGroupClassScheduleService(RecurringGroupClassScheduleRepository recurringGroupClassScheduleRepository,
                                              GymGroupClassOfferingService gymGroupClassOfferingService,
                                              GymGroupClassOfferingRepository gymGroupClassOfferingRepository) {
        this.recurringGroupClassScheduleRepository = recurringGroupClassScheduleRepository;
        this.gymGroupClassOfferingService = gymGroupClassOfferingService;
        this.gymGroupClassOfferingRepository = gymGroupClassOfferingRepository;
    }

    public ResponseRecurringGroupClassScheduleDTO addRecurringGroupClassSchedule(RequestRecurringGroupClassScheduleDTO dto) {

        RecurringGroupClassSchedule newGroupClasses = mapDtoToEntity(dto);

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

                //NOTE_FOR_ME: Dodaj sprawdzanei kolizji z pojedynczymi zajęciami
            }

            currentDate = currentDate.plusDays(1);
        }

        recurringGroupClassScheduleRepository.save(newGroupClasses);

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
