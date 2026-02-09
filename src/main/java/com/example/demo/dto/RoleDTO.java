package com.example.demo.dto;

import com.example.demo.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {

  private Long id;
  private String name;
  private Boolean isActive;

  public static RoleDTO fromEntity(Role role) {
    if (role == null) {
      return null;
    }
    return RoleDTO.builder()
        .id(role.getId())
        .name(role.getName())
        .isActive(role.getIsActive())
        .build();
  }
}
