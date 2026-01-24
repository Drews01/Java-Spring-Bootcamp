package com.example.demo.repository;

import com.example.demo.entity.Role;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

  @Query("SELECT r FROM Role r WHERE (r.deleted = false OR r.deleted IS NULL)")
  List<Role> findByDeletedFalse();

  @Query("SELECT r FROM Role r WHERE (r.deleted = false OR r.deleted IS NULL)")
  Page<Role> findByDeletedFalse(Pageable pageable);

  @Query("SELECT r FROM Role r WHERE r.name = :name AND (r.deleted = false OR r.deleted IS NULL)")
  Optional<Role> findByName(@Param("name") String name);
}
