package com.example.demo.entity;

import java.io.Serializable;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleMenuId implements Serializable {

  private static final long serialVersionUID = 1L;

  private Long roleId;
  private Long menuId;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RoleMenuId that = (RoleMenuId) o;
    return Objects.equals(roleId, that.roleId) && Objects.equals(menuId, that.menuId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(roleId, menuId);
  }
}
