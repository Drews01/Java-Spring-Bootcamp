package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_profile")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {

  @Id
  @Column(name = "user_id")
  private Long userId;

  @OneToOne
  @MapsId
  @JoinColumn(name = "user_id")
  private User user;

  @Column(length = 500)
  private String address;

  @Column(unique = true, length = 20)
  private String nik;

  @Column(name = "ktp_path", length = 255)
  private String ktpPath;

  @Column(name = "phone_number", length = 20)
  private String phoneNumber;

  @Column(name = "account_number", length = 50)
  private String accountNumber;

  @Column(name = "bank_name", length = 100)
  private String bankName;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }

  @PrePersist
  protected void onCreate() {
    updatedAt = LocalDateTime.now();
  }
}
