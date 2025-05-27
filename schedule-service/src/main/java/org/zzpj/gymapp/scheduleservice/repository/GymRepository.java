package org.zzpj.gymapp.scheduleservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zzpj.gymapp.scheduleservice.model.Gym;


@Repository
public interface GymRepository extends JpaRepository<Gym, Long> {
}
