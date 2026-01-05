package com.example.demo.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
  private Long userId;
  private String address;
  private String nik;
  private String ktpPath;
  private String phoneNumber;
  private LocalDateTime updatedAt;
}
