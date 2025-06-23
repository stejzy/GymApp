package org.zzpj.gymapp.scheduleservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "gyms")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Gym {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gym_id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "city")
    private String city;

    @Column(name = "address")
    private String address;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "opening_hour")
    private LocalTime openingHour;

    @Column(name = "closing_hour")
    private LocalTime closingHour;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "gym_trainer_ids", joinColumns = @JoinColumn(name = "gym_id"))
    @Column(name = "trainer_id")
    private List<Long> trainerIds;

    @OneToMany(mappedBy = "gym")
    private List<GymGroupClassOffering> groupClassOfferings;
}
