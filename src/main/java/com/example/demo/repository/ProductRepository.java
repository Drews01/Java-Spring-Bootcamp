package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Product;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("SELECT p FROM Product p WHERE p.code = :code")
    Optional<Product> findByCode(@Param("code") String code);

    @Query("SELECT p FROM Product p WHERE p.code = :code AND (p.deleted = false OR p.deleted IS NULL)")
    Optional<Product> findByCodeAndDeletedFalse(@Param("code") String code);

    List<Product> findByIsActive(Boolean isActive);

    @Query("SELECT p FROM Product p WHERE p.deleted = false OR p.deleted IS NULL")
    List<Product> findByDeletedFalse();

    @Query("SELECT p FROM Product p WHERE p.isActive = true AND (p.deleted = false OR p.deleted IS NULL)")
    List<Product> findByIsActiveTrueAndDeletedFalse();

    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE p.code = :code AND (p.deleted = false OR p.deleted IS NULL)")
    boolean existsByCodeAndDeletedFalse(@Param("code") String code);
}
