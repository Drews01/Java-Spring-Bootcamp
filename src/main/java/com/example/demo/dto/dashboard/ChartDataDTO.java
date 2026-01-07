package com.example.demo.dto.dashboard;

import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChartDataDTO {
  private Integer year;
  private Map<String, Double> monthlyStats; // e.g. "JANUARY" -> 50000.0
  private Map<Integer, Double> yearlyTrend; // e.g. 2023 -> 120000.0, 2024 -> 500000.0
}
