package com.example.demo.service;

import com.example.demo.dto.BranchDTO;
import com.example.demo.dto.CreateBranchRequest;
import com.example.demo.dto.UpdateBranchRequest;
import com.example.demo.dto.UserBranchDTO;
import com.example.demo.entity.Branch;
import com.example.demo.entity.User;
import com.example.demo.repository.BranchRepository;
import com.example.demo.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BranchService {

  private final BranchRepository branchRepository;
  private final UserRepository userRepository;

  /** Get all branches (including inactive) */
  public List<BranchDTO> getAllBranches() {
    return branchRepository.findAll().stream()
        .map(BranchDTO::fromEntity)
        .collect(Collectors.toList());
  }

  /** Get only active branches */
  public List<BranchDTO> getActiveBranches() {
    return branchRepository.findByIsActiveTrue().stream()
        .map(BranchDTO::fromEntity)
        .collect(Collectors.toList());
  }

  /** Get branch by ID */
  public BranchDTO getBranchById(Long id) {
    Branch branch =
        branchRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Branch not found with id: " + id));
    return BranchDTO.fromEntity(branch);
  }

  /** Create a new branch */
  @Transactional
  public BranchDTO createBranch(CreateBranchRequest request) {
    // Validate unique code
    if (branchRepository.existsByCode(request.code())) {
      throw new IllegalArgumentException("Branch code already exists: " + request.code());
    }

    // Validate unique name
    if (branchRepository.existsByName(request.name())) {
      throw new IllegalArgumentException("Branch name already exists: " + request.name());
    }

    Branch branch =
        Branch.builder()
            .code(request.code().toUpperCase())
            .name(request.name())
            .address(request.address())
            .isActive(true)
            .build();

    Branch savedBranch = branchRepository.save(branch);
    return BranchDTO.fromEntity(savedBranch);
  }

  /** Update an existing branch */
  @Transactional
  public BranchDTO updateBranch(Long id, UpdateBranchRequest request) {
    Branch branch =
        branchRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Branch not found with id: " + id));

    // Update name if provided and different
    if (request.name() != null && !request.name().equals(branch.getName())) {
      if (branchRepository.existsByName(request.name())) {
        throw new IllegalArgumentException("Branch name already exists: " + request.name());
      }
      branch.setName(request.name());
    }

    // Update address if provided
    if (request.address() != null) {
      branch.setAddress(request.address());
    }

    // Update isActive if provided
    if (request.isActive() != null) {
      branch.setIsActive(request.isActive());
    }

    Branch savedBranch = branchRepository.save(branch);
    return BranchDTO.fromEntity(savedBranch);
  }

  /** Deactivate a branch (soft delete) */
  @Transactional
  public void deactivateBranch(Long id) {
    Branch branch =
        branchRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Branch not found with id: " + id));

    branch.setIsActive(false);
    branchRepository.save(branch);
  }

  /** Assign a user to a branch */
  @Transactional
  public UserBranchDTO assignUserToBranch(Long userId, Long branchId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

    Branch branch =
        branchRepository
            .findById(branchId)
            .orElseThrow(() -> new RuntimeException("Branch not found with id: " + branchId));

    // Validate user has assignable role (MARKETING or BRANCH_MANAGER)
    boolean hasAssignableRole =
        user.getRoles().stream()
            .anyMatch(
                role ->
                    role.getName().equalsIgnoreCase("MARKETING")
                        || role.getName().equalsIgnoreCase("BRANCH_MANAGER"));

    if (!hasAssignableRole) {
      throw new IllegalArgumentException(
          "User must have MARKETING or BRANCH_MANAGER role to be assigned to a branch");
    }

    user.setBranch(branch);
    User savedUser = userRepository.save(user);
    return UserBranchDTO.fromUser(savedUser);
  }

  /** Unassign a user from their branch */
  @Transactional
  public UserBranchDTO unassignUserFromBranch(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

    user.setBranch(null);
    User savedUser = userRepository.save(user);
    return UserBranchDTO.fromUser(savedUser);
  }

  /** Get all users assigned to a branch */
  public List<UserBranchDTO> getUsersByBranch(Long branchId) {
    // Verify branch exists
    branchRepository
        .findById(branchId)
        .orElseThrow(() -> new RuntimeException("Branch not found with id: " + branchId));

    return userRepository.findByDeletedFalse().stream()
        .filter(user -> user.getBranch() != null && user.getBranch().getId().equals(branchId))
        .map(UserBranchDTO::fromUser)
        .collect(Collectors.toList());
  }

  /** Get all users that can be assigned to branches (MARKETING or BRANCH_MANAGER roles) */
  public List<UserBranchDTO> getAssignableUsers() {
    return userRepository.findByDeletedFalse().stream()
        .filter(
            user ->
                user.getRoles().stream()
                    .anyMatch(
                        role ->
                            role.getName().equalsIgnoreCase("MARKETING")
                                || role.getName().equalsIgnoreCase("BRANCH_MANAGER")))
        .map(UserBranchDTO::fromUser)
        .collect(Collectors.toList());
  }
}
