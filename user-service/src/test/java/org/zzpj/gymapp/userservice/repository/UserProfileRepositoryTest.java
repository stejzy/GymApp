package org.zzpj.gymapp.userservice.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.zzpj.gymapp.userservice.entity.UserProfile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserProfileRepositoryTest {

    @Autowired
    private UserProfileRepository userProfileRepository;

    private UserProfile userProfile1;
    private UserProfile userProfile2;

    @BeforeEach
    void setUp() {
        userProfileRepository.deleteAll();

        userProfile1 = new UserProfile();
        userProfile1.setUserId(1L);
        userProfile1.setFirstName("John");
        userProfile1.setLastName("Doe");
        userProfile1.setGender("Male");
        userProfile1.setHeight(180.0);
        userProfile1.setWeight(75.0);
        userProfile1.setBirthday(LocalDate.of(1990, 1, 1));
        userProfile1.setPhone("+48123456789");
        userProfile1.setBio("Test bio 1");
        userProfile1.setAvatarUrl("https://example.com/avatar1.jpg");

        userProfile2 = new UserProfile();
        userProfile2.setUserId(2L);
        userProfile2.setFirstName("Jane");
        userProfile2.setLastName("Smith");
        userProfile2.setGender("Female");
        userProfile2.setHeight(165.0);
        userProfile2.setWeight(60.0);
        userProfile2.setBirthday(LocalDate.of(1992, 5, 15));
        userProfile2.setPhone("+48987654321");
        userProfile2.setBio("Test bio 2");
        userProfile2.setAvatarUrl("https://example.com/avatar2.jpg");
    }

    @Test
    void shouldSaveUserProfile() {
        // When
        UserProfile savedProfile = userProfileRepository.save(userProfile1);

        // Then
        assertThat(savedProfile.getId()).isNotNull();
        assertThat(savedProfile.getUserId()).isEqualTo(1L);
        assertThat(savedProfile.getFirstName()).isEqualTo("John");
        assertThat(savedProfile.getLastName()).isEqualTo("Doe");
    }

    @Test
    void shouldFindUserProfileByUserId() {
        // Given
        userProfileRepository.save(userProfile1);

        // When
        Optional<UserProfile> foundProfile = userProfileRepository.findByUserId(1L);

        // Then
        assertThat(foundProfile).isPresent();
        assertThat(foundProfile.get().getUserId()).isEqualTo(1L);
        assertThat(foundProfile.get().getFirstName()).isEqualTo("John");
    }

    @Test
    void shouldReturnEmptyWhenUserProfileNotFound() {
        // When
        Optional<UserProfile> foundProfile = userProfileRepository.findByUserId(999L);

        // Then
        assertThat(foundProfile).isEmpty();
    }

    @Test
    void shouldCheckIfUserProfileExists() {
        // Given
        userProfileRepository.save(userProfile1);

        // When
        boolean exists = userProfileRepository.existsByUserId(1L);

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenUserProfileDoesNotExist() {
        // When
        boolean exists = userProfileRepository.existsByUserId(999L);

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void shouldFindUserProfilesByUserIdIn() {
        // Given
        userProfileRepository.save(userProfile1);
        userProfileRepository.save(userProfile2);

        // When
        List<UserProfile> foundProfiles = userProfileRepository.findByUserIdIn(List.of(1L, 2L));

        // Then
        assertThat(foundProfiles).hasSize(2);
        assertThat(foundProfiles).extracting("userId").containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void shouldReturnEmptyListWhenNoProfilesFound() {
        // When
        List<UserProfile> foundProfiles = userProfileRepository.findByUserIdIn(List.of(999L, 1000L));

        // Then
        assertThat(foundProfiles).isEmpty();
    }

    @Test
    void shouldDeleteUserProfileByUserId() {
        // Given
        userProfileRepository.save(userProfile1);
        assertThat(userProfileRepository.existsByUserId(1L)).isTrue();

        // When
        userProfileRepository.deleteByUserId(1L);

        // Then
        assertThat(userProfileRepository.existsByUserId(1L)).isFalse();
    }

    @Test
    void shouldUpdateUserProfile() {
        // Given
        UserProfile savedProfile = userProfileRepository.save(userProfile1);
        savedProfile.setFirstName("Updated");
        savedProfile.setLastName("Name");

        // When
        UserProfile updatedProfile = userProfileRepository.save(savedProfile);

        // Then
        assertThat(updatedProfile.getFirstName()).isEqualTo("Updated");
        assertThat(updatedProfile.getLastName()).isEqualTo("Name");
        assertThat(updatedProfile.getUserId()).isEqualTo(1L);
    }

    @Test
    void shouldFindAllUserProfiles() {
        // Given
        userProfileRepository.save(userProfile1);
        userProfileRepository.save(userProfile2);

        // When
        List<UserProfile> allProfiles = userProfileRepository.findAll();

        // Then
        assertThat(allProfiles).hasSize(2);
        assertThat(allProfiles).extracting("userId").containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void shouldDeleteUserProfileById() {
        // Given
        UserProfile savedProfile = userProfileRepository.save(userProfile1);
        Long profileId = savedProfile.getId();

        // When
        userProfileRepository.deleteById(profileId);

        // Then
        assertThat(userProfileRepository.findById(profileId)).isEmpty();
    }
} 