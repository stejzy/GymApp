package org.zzpj.gymapp.scheduleservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@Table(name = "recurring_group_class_schedule")
public class RecurringGroupClassSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recurring_group_class_schedule_id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "gym_group_class_offering_id")
    private GymGroupClassOffering gymGroupClassOffering;

    @OneToMany(mappedBy = "recurringGroupClassSchedule", cascade = CascadeType.ALL)
    private List<GroupClassSchedule> generatedSchedules = new ArrayList<>();

    @Column(name = "trainer_id", nullable = false)
    private Long trainerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Frequency frequency;

    @Column(nullable = false)
    private Integer capacity;
}
