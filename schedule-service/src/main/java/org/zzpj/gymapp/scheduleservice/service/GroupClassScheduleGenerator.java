package org.zzpj.gymapp.scheduleservice.service;

import org.springframework.stereotype.Service;
import org.zzpj.gymapp.scheduleservice.model.Frequency;
import org.zzpj.gymapp.scheduleservice.model.GroupClassSchedule;
import org.zzpj.gymapp.scheduleservice.model.RecurringGroupClassSchedule;
import org.zzpj.gymapp.scheduleservice.repository.GroupClassScheduleRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class GroupClassScheduleGenerator {

    private final GroupClassScheduleRepository groupClassScheduleRepository;

    public GroupClassScheduleGenerator(GroupClassScheduleRepository groupClassScheduleRepository) {
        this.groupClassScheduleRepository = groupClassScheduleRepository;
    }

    public void generateForRecurringSchedule(RecurringGroupClassSchedule recurring, LocalDate fromDate, LocalDate toDate) {
        // Ustal właściwe daty w obrębie zakresu recurring
        LocalDate start = fromDate.isBefore(recurring.getStartDate()) ? recurring.getStartDate() : fromDate;
        LocalDate end = toDate.isAfter(recurring.getEndDate()) ? recurring.getEndDate() : toDate;

        LocalDate current = start;

        // Określ ile tygodni przesuwać po wygenerowaniu zajęć (1 lub 2)
        int weeksToAdd = 1;
        if (recurring.getFrequency() == Frequency.BIWEEKLY) {
            weeksToAdd = 2;
        }

        // Znajdź pierwszy dzień, który jest właściwym dniem tygodnia
        while (current.getDayOfWeek() != recurring.getDayOfWeek()) {
            current = current.plusDays(1);
            if (current.isAfter(end)) {
                return; // Nie ma już właściwych dni w zakresie
            }
        }

        // Generuj zajęcia co tydzień lub co dwa tygodnie, aż do końca zakresu
        while (!current.isAfter(end)) {
            LocalDateTime fullStart = LocalDateTime.of(current, recurring.getStartTime());
            LocalDateTime fullEnd = LocalDateTime.of(current, recurring.getEndTime());

            // Sprawdź czy już istnieje takie zajęcie
            boolean exists = groupClassScheduleRepository
                    .existsByRecurringGroupClassScheduleAndStartTime(recurring, fullStart);

            if (!exists) {
                // Sprawdź kolizje z innymi zajęciami
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
                }
            }

            // Przesuń o określoną liczbę tygodni
            current = current.plusWeeks(weeksToAdd);
        }
    }
}

