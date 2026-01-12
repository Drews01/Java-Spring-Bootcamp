package com.example.demo.dto;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserListDTO {

  private Long id;
  private String username;
  private String email;
  private Boolean isActive;
  private Set<String> roles;
  private String branchName;

  public static UserListDTO fromUser(com.example.demo.entity.User user) {
    Set<String> roleNames =
        user.getRoles().stream()
            .map(role -> role.getName())
            .collect(java.util.stream.Collectors.toSet());
    return UserListDTO.builder()
        .id(user.getId())
        .username(user.getUsername())
        .email(user.getEmail())
        .isActive(user.getIsActive())
        .roles(roleNames)
        .branchName(user.getBranch() != null ? user.getBranch().getName() : null)
        .build();
  }
}
