package com.example.demo.enums;

public enum RoleName {
  USER,
  ADMIN,
  MARKETING,
  BRANCH_MANAGER,
  BACK_OFFICE;

  public String getRoleName() {
    return this.name();
  }
}
