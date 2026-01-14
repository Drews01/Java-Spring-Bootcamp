package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionHistoryRequestDTO {
  private Integer month; // Optional: 1-12
  private Integer year; // Optional: e.g., 2026
  @Builder.Default private Integer page = 0; // Optional: for pagination (default: 0)
  @Builder.Default private Integer size = 20; // Optional: items per page (default: 20)
}
