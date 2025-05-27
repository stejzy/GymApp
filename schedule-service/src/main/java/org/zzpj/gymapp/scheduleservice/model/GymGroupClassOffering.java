package org.zzpj.gymapp.scheduleservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "gym_group_class_offering")
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
