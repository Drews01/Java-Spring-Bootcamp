package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "role_menus")
@IdClass(RoleMenuId.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleMenu {

  @Id
  @Column(name = "role_id")
  private Long roleId;

  @Id
  @Column(name = "menu_id")
  private Long menuId;

  @ManyToOne
  @JoinColumn(name = "role_id", insertable = false, updatable = false)
  private Role role;

  @ManyToOne
  @JoinColumn(name = "menu_id", insertable = false, updatable = false)
  private Menu menu;

  @Column(name = "is_active")
  @Builder.Default
  private Boolean isActive = true;

  @Column(name = "is_deleted")
  @Builder.Default
  private Boolean deleted = false;

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
