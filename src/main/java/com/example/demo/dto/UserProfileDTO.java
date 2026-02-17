package com.example.demo.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
  private String username;
  private String email;

  @Size(max = 255, message = "address must not exceed 255 characters")
  private String address;

  @Size(min = 16, max = 16, message = "nik must be exactly 16 characters")
  @Pattern(regexp = "^[0-9]*$", message = "nik must contain only digits")
  private String nik;

  private String ktpPath;

  @Size(max = 20, message = "phoneNumber must not exceed 20 characters")
  @Pattern(
      regexp = "^[+]?[0-9]*$",
      message = "phoneNumber must contain only digits and optional leading +")
  private String phoneNumber;

  @Size(max = 50, message = "accountNumber must not exceed 50 characters")
  private String accountNumber;

  @Size(max = 100, message = "bankName must not exceed 100 characters")
  private String bankName;

  private LocalDateTime updatedAt;
}
