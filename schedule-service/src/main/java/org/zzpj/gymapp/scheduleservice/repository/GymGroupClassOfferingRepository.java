package org.zzpj.gymapp.scheduleservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zzpj.gymapp.scheduleservice.model.Gym;
import org.zzpj.gymapp.scheduleservice.model.GymGroupClassOffering;

import java.util.List;

@Repository
public interface GymGroupClassOfferingRepository extends JpaRepository<GymGroupClassOffering, Long> {
    List<GymGroupClassOffering> findByGym(Gym gym);
}
