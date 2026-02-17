package com.example.demo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionHistoryRequestDTO {
  @Min(value = 1, message = "month must be between 1 and 12")
  @Max(value = 12, message = "month must be between 1 and 12")
  private Integer month; // Optional: 1-12

  @Min(value = 1900, message = "year must be at least 1900")
  private Integer year; // Optional: e.g., 2026

  @Min(value = 0, message = "page must be at least 0")
  @Builder.Default
  private Integer page = 0; // Optional: for pagination (default: 0)

  @Min(value = 1, message = "size must be at least 1")
  @Max(value = 100, message = "size must not exceed 100")
  @Builder.Default
  private Integer size = 20; // Optional: items per page (default: 20)
}
