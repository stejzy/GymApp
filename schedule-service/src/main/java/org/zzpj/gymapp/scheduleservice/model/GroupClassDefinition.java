package org.zzpj.gymapp.scheduleservice.model;

import jakarta.persistence.*;

@Entity
public class GroupClassDefinition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_class_definition_id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

}
