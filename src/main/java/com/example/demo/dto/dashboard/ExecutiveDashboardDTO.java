package com.example.demo.dto.dashboard;

import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExecutiveDashboardDTO {

  // Section 1: Funnel & Volume
  private Map<String, Long> statusCounts;
  private Long totalApplications;
  private Double approvalRate; // Percentage (0-100)

  // Section 2: Financial Overview
  private Double totalDisbursedAmount;
  private Double totalInterestEarned;
  private Double outstandingPrincipal;
  private Double potentialRevenue;

  // Section 3: Trends
  private ChartDataDTO disbursementTrend;
}
