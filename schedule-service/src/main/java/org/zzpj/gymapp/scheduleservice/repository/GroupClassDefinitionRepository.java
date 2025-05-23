package org.zzpj.gymapp.scheduleservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zzpj.gymapp.scheduleservice.model.GroupClassDefinition;

@Repository
public interface GroupClassDefinitionRepository extends JpaRepository<GroupClassDefinition, Long> {
}
