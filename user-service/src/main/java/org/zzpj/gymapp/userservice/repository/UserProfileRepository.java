package org.zzpj.gymapp.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zzpj.gymapp.userservice.entity.UserProfile;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
} 