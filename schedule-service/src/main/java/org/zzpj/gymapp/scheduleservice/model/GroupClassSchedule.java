package org.zzpj.gymapp.scheduleservice.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
public class GroupClassSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "gym_group_class_offering_id")
    private GymGroupClassOffering gymGroupClassOffering;

    @ManyToOne
    @JoinColumn(name = "recurring_schedule_id")
    private RecurringGroupClassSchedule recurringGroupClassSchedule;

    @Column(name = "trainer_id", nullable = false)
    private Long trainerId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @ElementCollection
    @CollectionTable(
            name = "group_class_participant_ids",
            joinColumns = @JoinColumn(name = "group_class_schedule_id")
    )
    @Column(name = "user_id")
    private List<Long> participantIds = new ArrayList<>();

    @Column(nullable = false)
    private Integer capacity;
}

