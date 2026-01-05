package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 50)
  private String code;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(nullable = false, name = "interest_rate")
  private Double interestRate;

  @Column(nullable = false, name = "interest_rate_type", length = 20)
  private String interestRateType;

  @Column(nullable = false, name = "min_amount")
  private Double minAmount;

  @Column(nullable = false, name = "max_amount")
  private Double maxAmount;

  @Column(nullable = false, name = "min_tenure_months")
  private Integer minTenureMonths;

  @Column(nullable = false, name = "max_tenure_months")
  private Integer maxTenureMonths;

  @Column(nullable = false, name = "is_active")
  @Builder.Default
  private Boolean isActive = true;

  @Column(name = "is_deleted")
  @Builder.Default
  private Boolean deleted = false;

  @Column(nullable = false, name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @Column(nullable = false, name = "updated_at")
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
    if (deleted == null) {
      deleted = false;
    }
    if (isActive == null) {
      isActive = true;
    }
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
