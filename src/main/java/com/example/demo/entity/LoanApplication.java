package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "loan_applications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplication {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "loan_application_id")
  private Long loanApplicationId;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @Column(nullable = false)
  private Double amount;

  @Column(name = "tenure_months", nullable = false)
  private Integer tenureMonths;

  @Column(name = "interest_rate_applied", nullable = false)
  private Double interestRateApplied;

  @Column(name = "total_amount_to_pay")
  private Double totalAmountToPay; // Total amount to be paid (principal + interest)

  @Column(name = "current_status", nullable = false, length = 50)
  private String currentStatus;

  @Column(name = "is_paid")
  @Builder.Default
  private Boolean isPaid = false;

  @Column(name = "paid_at")
  private LocalDateTime paidAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
