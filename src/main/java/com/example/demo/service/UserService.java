package com.example.demo.service;

import com.example.demo.dto.AdminCreateUserRequest;
import com.example.demo.dto.UserListDTO;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;
  private final EmailService emailService;
  private final PasswordResetService passwordResetService;

  public User createUser(User user) {
    return userRepository.save(user);
  }

  public List<User> getAllUsers() {
    return userRepository.findByDeletedFalse();
  }

  public User getUserById(Long id) {
    return userRepository
        .findById(id)
        .filter(u -> u.getDeleted() == null || !u.getDeleted())
        .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
  }

  public User updateUser(Long id, User userDetails) {
    User user = getUserById(id);
    user.setUsername(userDetails.getUsername());
    user.setEmail(userDetails.getEmail());
    user.setIsActive(userDetails.getIsActive());
    if (userDetails.getRoles() != null) {
      user.setRoles(userDetails.getRoles());
    }
    return userRepository.save(user);
  }

  public void deleteUser(Long id) {
    User user = getUserById(id);
    user.setDeleted(true);
    user.setIsActive(false);
    userRepository.save(user);
  }

  public Page<UserListDTO> getAllUsersForAdmin(Pageable pageable) {
    return userRepository.findByDeletedFalse(pageable).map(UserListDTO::fromUser);
  }

  // ============= ADMIN USER MANAGEMENT =============

  /** Admin: Set user active/inactive status */
  @Transactional
  public UserListDTO setUserActiveStatus(Long userId, Boolean isActive, Long currentAdminId) {
    User user = getUserById(userId);

    // IDOR Protection: Prevent self-deactivation
    if (userId.equals(currentAdminId) && Boolean.FALSE.equals(isActive)) {
      throw new IllegalArgumentException("Cannot deactivate your own account");
    }

    // IDOR Protection: Protect last admin - cannot deactivate if last active admin
    if (Boolean.FALSE.equals(isActive) && isUserAdmin(user)) {
      long activeAdminCount = countActiveAdmins();
      if (activeAdminCount <= 1) {
        throw new IllegalArgumentException(
            "Cannot deactivate the last active admin. System must have at least one admin.");
      }
    }

    user.setIsActive(isActive);
    User savedUser = userRepository.save(user);
    return UserListDTO.fromUser(savedUser);
  }

  /** Admin: Update user's roles */
  @Transactional
  public UserListDTO updateUserRoles(Long userId, Set<String> roleNames, Long currentAdminId) {
    User user = getUserById(userId);

    // IDOR Protection: Prevent self-role-removal of ADMIN role
    boolean currentUserIsTarget = userId.equals(currentAdminId);
    boolean removingAdminRole =
        isUserAdmin(user) && !roleNames.stream().anyMatch(r -> r.equalsIgnoreCase("ADMIN"));

    if (currentUserIsTarget && removingAdminRole) {
      throw new IllegalArgumentException("Cannot remove ADMIN role from your own account");
    }

    // IDOR Protection: Protect last admin - cannot remove ADMIN role if last admin
    if (removingAdminRole) {
      long adminCount = countActiveAdmins();
      if (adminCount <= 1) {
        throw new IllegalArgumentException(
            "Cannot remove ADMIN role from the last admin. System must have at least one admin.");
      }
    }

    Set<Role> roles = new HashSet<>();
    for (String roleName : roleNames) {
      Role role =
          roleRepository
              .findByName(roleName.toUpperCase())
              .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
      roles.add(role);
    }

    user.setRoles(roles);
    User savedUser = userRepository.save(user);
    return UserListDTO.fromUser(savedUser);
  }

  /** Check if user has ADMIN role */
  private boolean isUserAdmin(User user) {
    return user.getRoles().stream().anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getName()));
  }

  /** Count active users with ADMIN role */
  private long countActiveAdmins() {
    return userRepository.findByDeletedFalse().stream()
        .filter(u -> Boolean.TRUE.equals(u.getIsActive()))
        .filter(this::isUserAdmin)
        .count();
  }

  /** Admin: Create new user with roles (no password - user resets via email) */
  @Transactional
  public UserListDTO createUserByAdmin(AdminCreateUserRequest request) {
    // Validate username uniqueness
    if (userRepository.existsByUsername(request.username())) {
      throw new IllegalArgumentException("Username already exists");
    }

    // Validate email uniqueness
    if (userRepository.existsByEmail(request.email())) {
      throw new IllegalArgumentException("Email already exists");
    }

    // Get roles
    Set<Role> roles = new HashSet<>();
    if (request.roleNames() != null && !request.roleNames().isEmpty()) {
      for (String roleName : request.roleNames()) {
        Role role =
            roleRepository
                .findByName(roleName.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
        roles.add(role);
      }
    } else {
      // Default to USER role if no roles specified
      Role userRole =
          roleRepository
              .findByName("USER")
              .orElseThrow(() -> new RuntimeException("Default USER role not found"));
      roles.add(userRole);
    }

    // Generate random temp password (user cannot login with this)
    String tempPassword = UUID.randomUUID().toString();

    // Create user
    User user =
        User.builder()
            .username(request.username())
            .email(request.email())
            .password(passwordEncoder.encode(tempPassword))
            .isActive(true)
            .roles(roles)
            .build();

    User savedUser = userRepository.save(user);

    // Send password reset email so user can set their own password
    sendWelcomePasswordResetEmail(savedUser);

    return UserListDTO.fromUser(savedUser);
  }

  private void sendWelcomePasswordResetEmail(User user) {
    // Create password reset token (valid for 24 hours for new users)
    String token = passwordResetService.createPasswordResetToken(user.getId(), 24);

    String resetLink = "http://localhost:8080/reset-password?token=" + token;
    String emailBody =
        "Welcome to the system!\n\n"
            + "Your account has been created with username: "
            + user.getUsername()
            + "\n\n"
            + "Please click the link below to set your password:\n"
            + resetLink
            + "\n\n"
            + "This link will expire in 24 hours.";

    emailService.sendSimpleMessage(user.getEmail(), "Welcome - Set Your Password", emailBody);
  }
}
