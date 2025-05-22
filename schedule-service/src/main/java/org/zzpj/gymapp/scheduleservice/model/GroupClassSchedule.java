package org.zzpj.gymapp.scheduleservice.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class GroupClassSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "gym_group_class_offering_id")
    private GymGroupClassOffering gymGroupClassOffering;

    @Column(name = "trainer_id", nullable = false)
    private Long trainerId;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @ElementCollection
    @CollectionTable(
            name = "group_class_participant_ids",
            joinColumns = @JoinColumn(name = "group_class_schedule_id")
    )
    @Column(name = "user_id")
    private List<Long> participantIds = new ArrayList<>();
}
