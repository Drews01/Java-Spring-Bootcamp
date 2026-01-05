package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "user_products",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProduct {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_product_id")
  private Long userProductId;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @Column(nullable = false, length = 20)
  @Builder.Default
  private String status = "ACTIVE"; // ACTIVE / INACTIVE

  @Column(name = "current_used_amount")
  @Builder.Default
  private Double currentUsedAmount = 0.0; // Total outstanding (unpaid) loan amounts

  @Column(name = "total_paid_amount")
  @Builder.Default
  private Double totalPaidAmount = 0.0; // Cumulative paid amounts (for tier upgrades)

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }
}
