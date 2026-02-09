package com.example.demo.dto;

import com.example.demo.entity.RoleMenu;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleMenuDTO {

  private Long roleId;
  private String roleName;
  private Long menuId;
  private String menuName;
  private String menuCode;
  private Boolean isActive;

  public static RoleMenuDTO fromEntity(RoleMenu roleMenu) {
    if (roleMenu == null) {
      return null;
    }
    return RoleMenuDTO.builder()
        .roleId(roleMenu.getRoleId())
        .roleName(roleMenu.getRole() != null ? roleMenu.getRole().getName() : null)
        .menuId(roleMenu.getMenuId())
        .menuName(roleMenu.getMenu() != null ? roleMenu.getMenu().getName() : null)
        .menuCode(roleMenu.getMenu() != null ? roleMenu.getMenu().getCode() : null)
        .isActive(roleMenu.getIsActive())
        .build();
  }
}
