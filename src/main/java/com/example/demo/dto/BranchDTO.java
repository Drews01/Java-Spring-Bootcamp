package com.example.demo.dto;

import com.example.demo.entity.Branch;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchDTO {

  private Long id;
  private String code;
  private String name;
  private String address;
  private Boolean isActive;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public static BranchDTO fromEntity(Branch branch) {
    return BranchDTO.builder()
        .id(branch.getId())
        .code(branch.getCode())
        .name(branch.getName())
        .address(branch.getAddress())
        .isActive(branch.getIsActive())
        .createdAt(branch.getCreatedAt())
        .updatedAt(branch.getUpdatedAt())
        .build();
  }
}
