package org.zzpj.gymapp.scheduleservice.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class GymGroupClassOffering {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "gym_id", nullable = false)
    private Gym gym;

    @ManyToOne
    @JoinColumn(name = "group_class_definition_id", nullable = false)
    private GroupClassDefinition groupClassDefinition;

    @OneToMany(mappedBy = "gymGroupClassOffering", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecurringGroupClassSchedule> recurringGroupClasses;

    @OneToMany(mappedBy = "gymGroupClassOffering", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupClassSchedule> scheduledClasses;
}
