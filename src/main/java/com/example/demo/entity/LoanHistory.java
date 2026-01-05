package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "loan_history",
    indexes =
        @Index(
            name = "idx_loan_history_loan_created",
            columnList = "loan_application_id, created_at"))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "loan_history_id")
  private Long loanHistoryId;

  @ManyToOne
  @JoinColumn(name = "loan_application_id", nullable = false)
  private LoanApplication loanApplication;

  @ManyToOne
  @JoinColumn(name = "actor_user_id", nullable = false)
  private User actorUser;

  @Column(nullable = false, length = 50)
  private String action; // SUBMIT, COMMENT_FORWARD, COMMENT, APPROVE, REJECT, DISBURSE

  @Column(columnDefinition = "TEXT")
  private String comment;

  @Column(name = "from_status", length = 50)
  private String fromStatus;

  @Column(name = "to_status", length = 50)
  private String toStatus;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }
}
