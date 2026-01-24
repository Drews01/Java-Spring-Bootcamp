package com.example.demo.repository;

import com.example.demo.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  @Query("SELECT u FROM User u WHERE (u.deleted = false OR u.deleted IS NULL)")
  List<User> findByDeletedFalse();

  @Query("SELECT u FROM User u WHERE (u.deleted = false OR u.deleted IS NULL)")
  Page<User> findByDeletedFalse(Pageable pageable);

  @Query(
      "SELECT u FROM User u WHERE u.username = :username AND (u.deleted = false OR u.deleted IS NULL)")
  Optional<User> findByUsername(@Param("username") String username);

  @Query("SELECT u FROM User u WHERE u.email = :email AND (u.deleted = false OR u.deleted IS NULL)")
  Optional<User> findByEmail(@Param("email") String email);

  @Query(
      "SELECT COUNT(u) > 0 FROM User u WHERE u.username = :username AND (u.deleted = false OR u.deleted IS NULL)")
  boolean existsByUsername(@Param("username") String username);

  @Query(
      "SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND (u.deleted = false OR u.deleted IS NULL)")
  boolean existsByEmail(@Param("email") String email);

  @Query(
      "SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName AND (u.deleted = false OR u.deleted IS NULL)")
  List<User> findByRoles_Name(@Param("roleName") String roleName);
}
