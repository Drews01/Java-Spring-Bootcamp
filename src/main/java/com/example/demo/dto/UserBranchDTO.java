package com.example.demo.dto;

import com.example.demo.entity.User;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBranchDTO {

  private Long userId;
  private String username;
  private String email;
  private Long branchId;
  private String branchName;
  private Set<String> roles;

  public static UserBranchDTO fromUser(User user) {
    Set<String> roleNames =
        user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toSet());
    return UserBranchDTO.builder()
        .userId(user.getId())
        .username(user.getUsername())
        .email(user.getEmail())
        .branchId(user.getBranch() != null ? user.getBranch().getId() : null)
        .branchName(user.getBranch() != null ? user.getBranch().getName() : null)
        .roles(roleNames)
        .build();
  }
}
