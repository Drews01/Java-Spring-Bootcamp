package com.example.demo.repository;

import com.example.demo.entity.Branch;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {

  Optional<Branch> findByCode(String code);

  Optional<Branch> findByName(String name);

  List<Branch> findByIsActiveTrue();

  boolean existsByCode(String code);

  boolean existsByName(String name);
}
