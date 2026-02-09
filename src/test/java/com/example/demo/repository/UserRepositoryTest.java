package com.example.demo.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.entity.User;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration tests for {@link UserRepository}.
 *
 * <p>Tests repository methods for user management including finding by username and email.
 */
@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

  @Autowired private TestEntityManager entityManager;

  @Autowired private UserRepository userRepository;

  private User testUser;

  @BeforeEach
  void setUp() {
    testUser =
        User.builder()
            .username("testuser")
            .email("test@example.com")
            .password("encodedPassword123")
            .isActive(true)
            .build();
    entityManager.persist(testUser);
    entityManager.flush();
  }

  @Test
  @DisplayName("Should find user by username")
  void findByUsername_shouldReturnUser() {
    // When
    Optional<User> found = userRepository.findByUsername("testuser");

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getEmail()).isEqualTo("test@example.com");
  }

  @Test
  @DisplayName("Should find user by email")
  void findByEmail_shouldReturnUser() {
    // When
    Optional<User> found = userRepository.findByEmail("test@example.com");

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getUsername()).isEqualTo("testuser");
  }

  @Test
  @DisplayName("Should return true when username exists")
  void existsByUsername_shouldReturnTrue_whenExists() {
    // When
    boolean exists = userRepository.existsByUsername("testuser");

    // Then
    assertThat(exists).isTrue();
  }

  @Test
  @DisplayName("Should return false when username does not exist")
  void existsByUsername_shouldReturnFalse_whenNotExists() {
    // When
    boolean exists = userRepository.existsByUsername("nonexistent");

    // Then
    assertThat(exists).isFalse();
  }

  @Test
  @DisplayName("Should return true when email exists")
  void existsByEmail_shouldReturnTrue_whenExists() {
    // When
    boolean exists = userRepository.existsByEmail("test@example.com");

    // Then
    assertThat(exists).isTrue();
  }
}
