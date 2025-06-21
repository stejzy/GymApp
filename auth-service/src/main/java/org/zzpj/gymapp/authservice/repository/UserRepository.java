package org.zzpj.gymapp.authservice.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zzpj.gymapp.authservice.entity.User;
import org.zzpj.gymapp.authservice.entity.Role;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Query("SELECT u.id FROM User u JOIN u.roles r WHERE r = :role")
    List<Long> findUserIdsByRole(@Param("role") Role role, Pageable pageable);
}
