package com.example.demo.repository;

import com.example.demo.entity.UserProduct;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProductRepository extends JpaRepository<UserProduct, Long> {

  List<UserProduct> findByUser_Id(Long userId);

  List<UserProduct> findByProduct_Id(Long productId);

  Optional<UserProduct> findByUser_IdAndProduct_Id(Long userId, Long productId);

  List<UserProduct> findByUser_IdAndStatus(Long userId, String status);

  List<UserProduct> findByStatus(String status);

  // Tier system queries
  @Query(
      "SELECT up FROM UserProduct up "
          + "WHERE up.user.id = :userId AND up.status = 'ACTIVE' "
          + "ORDER BY up.product.tierOrder DESC")
  List<UserProduct> findActiveUserProductsByUserIdOrderByTier(@Param("userId") Long userId);
}
