package com.example.demo.service;

import com.example.demo.dto.AdminCreateUserRequest;
import com.example.demo.dto.UserListDTO;
import com.example.demo.entity.User;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * User Service Interface.
 *
 * <p>Defines the contract for user management operations including:
 *
 * <ul>
 *   <li>User CRUD operations
 *   <li>User listing and pagination
 *   <li>Admin user management (status, roles)
 *   <li>User creation by admin
 * </ul>
 *
 * @author Java Spring Bootcamp
 * @version 1.0
 */
public interface IUserService {

  /**
   * Creates a new user.
   *
   * @param user the user entity to create
   * @return UserListDTO containing the created user information
   */
  UserListDTO createUser(User user);

  /**
   * Retrieves all non-deleted users.
   *
   * @return list of UserListDTO
   */
  List<UserListDTO> getAllUsers();

  /**
   * Retrieves a user by ID.
   *
   * @param id the user ID
   * @return UserListDTO containing the user information
   */
  UserListDTO getUserById(Long id);

  /**
   * Updates an existing user.
   *
   * @param id the user ID to update
   * @param userDetails the updated user details
   * @return UserListDTO containing the updated user information
   */
  UserListDTO updateUser(Long id, User userDetails);

  /**
   * Soft deletes a user by ID.
   *
   * @param id the user ID to delete
   */
  void deleteUser(Long id);

  /**
   * Retrieves all non-deleted users with pagination (for admin).
   *
   * @param pageable pagination parameters
   * @return Page of UserListDTO
   */
  Page<UserListDTO> getAllUsersForAdmin(Pageable pageable);

  /**
   * Sets a user's active/inactive status.
   *
   * @param userId the user ID to update
   * @param isActive the new active status
   * @param currentAdminId the ID of the admin performing the action
   * @return UserListDTO containing the updated user information
   */
  UserListDTO setUserActiveStatus(Long userId, Boolean isActive, Long currentAdminId);

  /**
   * Updates a user's roles.
   *
   * @param userId the user ID to update
   * @param roleNames the set of role names to assign
   * @param currentAdminId the ID of the admin performing the action
   * @return UserListDTO containing the updated user information
   */
  UserListDTO updateUserRoles(Long userId, Set<String> roleNames, Long currentAdminId);

  /**
   * Creates a new user by admin with specified roles.
   *
   * @param request the admin create user request
   * @return UserListDTO containing the created user information
   */
  UserListDTO createUserByAdmin(AdminCreateUserRequest request);
}
