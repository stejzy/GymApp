package org.zzpj.gymapp.scheduleservice.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.zzpj.gymapp.scheduleservice.model.RecurringGroupClassSchedule;
import org.zzpj.gymapp.scheduleservice.repository.RecurringGroupClassScheduleRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class RecurringScheduleScheduler {

    private final RecurringGroupClassScheduleRepository recurringRepository;
    private final GroupClassScheduleGenerator groupClassScheduleGenerator;

    public RecurringScheduleScheduler(RecurringGroupClassScheduleRepository recurringRepository,
                                      GroupClassScheduleGenerator groupClassScheduleGenerator) {
        this.recurringRepository = recurringRepository;
        this.groupClassScheduleGenerator = groupClassScheduleGenerator;
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void generateSchedulesForNextMonth() {
        LocalDate now = LocalDate.now().plusDays(1);
        LocalDate oneMonthAhead = now.plusMonths(1);

        List<RecurringGroupClassSchedule> activeSchedules = recurringRepository.findActiveSchedules(now, oneMonthAhead);

        for (RecurringGroupClassSchedule recurring : activeSchedules) {
            groupClassScheduleGenerator.generateForRecurringSchedule(recurring, now, oneMonthAhead);
        }
    }
}

