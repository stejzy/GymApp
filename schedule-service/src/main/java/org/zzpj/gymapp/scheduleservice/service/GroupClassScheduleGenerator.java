package org.zzpj.gymapp.scheduleservice.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.zzpj.gymapp.scheduleservice.exeption.ScheduleConflictException;
import org.zzpj.gymapp.scheduleservice.model.*;
import org.zzpj.gymapp.scheduleservice.repository.GroupClassScheduleRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class GroupClassScheduleGenerator {

    private final GroupClassScheduleRepository groupClassScheduleRepository;
    private final TrainingSessionService trainingSessionService;

    public GroupClassScheduleGenerator(GroupClassScheduleRepository groupClassScheduleRepository,
                                       TrainingSessionService trainingSessionService) {
        this.groupClassScheduleRepository = groupClassScheduleRepository;
        this.trainingSessionService = trainingSessionService;
    }

    @Transactional
    public void generateForRecurringSchedule(RecurringGroupClassSchedule recurring, LocalDate fromDate, LocalDate toDate) {
        LocalDate start = fromDate.isBefore(recurring.getStartDate()) ? recurring.getStartDate() : fromDate;
        LocalDate end = toDate.isAfter(recurring.getEndDate()) ? recurring.getEndDate() : toDate;

        LocalDate current = start;

        int weeksToAdd = 1;
        if (recurring.getFrequency() == Frequency.BIWEEKLY) {
            weeksToAdd = 2;
        }

        while (current.getDayOfWeek() != recurring.getDayOfWeek()) {
            current = current.plusDays(1);
            if (current.isAfter(end)) {
                return;
            }
        }

        while (!current.isAfter(end)) {
            LocalDateTime fullStart = LocalDateTime.of(current, recurring.getStartTime());
            LocalDateTime fullEnd = LocalDateTime.of(current, recurring.getEndTime());

            boolean exists = groupClassScheduleRepository
                    .existsByRecurringGroupClassScheduleAndStartTime(recurring, fullStart);

            if (!exists) {
                List<GroupClassSchedule> conflicts = groupClassScheduleRepository.findConflictingGroupSchedules(
                        recurring.getGymGroupClassOffering().getGym().getId(),
                        fullStart, fullEnd
                );

                if (conflicts.isEmpty()) {
                    GroupClassSchedule newSchedule = new GroupClassSchedule();
                    newSchedule.setRecurringGroupClassSchedule(recurring);
                    newSchedule.setStartTime(fullStart);
                    newSchedule.setEndTime(fullEnd);
                    newSchedule.setTrainerId(recurring.getTrainerId());
                    newSchedule.setCapacity(recurring.getCapacity());
                    newSchedule.setGymGroupClassOffering(recurring.getGymGroupClassOffering());

                    groupClassScheduleRepository.save(newSchedule);

                    TrainingSession session = new TrainingSession();
                    session.setTrainerId(recurring.getTrainerId());
                    session.setUserId(null);
                    session.setStartTime(fullStart);
                    session.setEndTime(fullEnd);
                    session.setClassId(newSchedule.getId());
                    session.setType(SessionType.GROUP);

                    // Jeśli utworzenie sesji rzuci wyjątek, wszystko się wycofa, łącznie z zapisem newSchedule
                    trainingSessionService.createTrainingSession(session);
                }
            }

            current = current.plusWeeks(weeksToAdd);
        }
    }
}

