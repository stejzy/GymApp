package org.zzpj.gymapp.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zzpj.gymapp.userservice.entity.UserProfile;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByUserId(Long userId);
}