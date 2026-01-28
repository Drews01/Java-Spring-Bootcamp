package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@EqualsAndHashCode(exclude = "roles")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String username;

  @Column(unique = true, nullable = false)
  private String email;

  @Column(nullable = true)
  @JsonIgnore
  private String password;

  @Enumerated(EnumType.STRING)
  @Column(name = "auth_provider")
  @Builder.Default
  private AuthProvider authProvider = AuthProvider.LOCAL;

  @Column(name = "is_active")
  @Builder.Default
  private Boolean isActive = true;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "user_roles",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id"))
  @JsonIgnoreProperties("users")
  @Builder.Default
  private Set<Role> roles = new HashSet<>();

  @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
  @JsonIgnore
  private UserProfile userProfile;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "branch_id")
  private Branch branch;

  @Column(name = "is_deleted")
  @Builder.Default
  private Boolean deleted = false;

  @Column(name = "last_password_reset_date")
  private LocalDateTime lastPasswordResetDate;

  @PrePersist
  protected void onCreate() {
    if (deleted == null) {
      deleted = false;
    }
    if (isActive == null) {
      isActive = true;
    }
  }
}
