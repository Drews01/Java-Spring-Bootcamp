package com.example.demo.service;

import com.example.demo.dto.dashboard.ChartDataDTO;
import com.example.demo.dto.dashboard.ExecutiveDashboardDTO;
import com.example.demo.enums.LoanStatus;
import com.example.demo.repository.LoanApplicationRepository;
import com.example.demo.repository.LoanHistoryRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

  private final LoanApplicationRepository loanApplicationRepository;
  private final LoanHistoryRepository loanHistoryRepository;

  public ExecutiveDashboardDTO getExecutiveDashboard(Integer year) {
    if (year == null) {
      year = java.time.Year.now().getValue();
    }

    // 1. Funnel & Volume (Cohort Analysis: Loans CREATED in this year)
    Map<String, Long> statusCounts = new HashMap<>();
    statusCounts.put(
        "SUBMITTED",
        loanApplicationRepository.countByCurrentStatusAndYear(LoanStatus.SUBMITTED.name(), year));
    statusCounts.put(
        "WAITING_APPROVAL",
        loanApplicationRepository.countByCurrentStatusAndYear(
            LoanStatus.WAITING_APPROVAL.name(), year));
    statusCounts.put(
        "APPROVED_WAITING_DISBURSEMENT",
        loanApplicationRepository.countByCurrentStatusAndYear(
            LoanStatus.APPROVED_WAITING_DISBURSEMENT.name(), year));
    statusCounts.put(
        "REJECTED",
        loanApplicationRepository.countByCurrentStatusAndYear(LoanStatus.REJECTED.name(), year));
    statusCounts.put(
        "DISBURSED",
        loanApplicationRepository.countByCurrentStatusAndYear(LoanStatus.DISBURSED.name(), year));

    Long totalApplications = loanApplicationRepository.countTotalApplicationsByYear(year);

    // Calculate Approval Rate: Approved / (Approved + Rejected)
    long approvedCount = statusCounts.get("DISBURSED")
        + statusCounts.get("APPROVED_WAITING_DISBURSEMENT"); // Approvals include pending disbursement
    long rejectedCount = statusCounts.get("REJECTED");
    long decisionedCount = approvedCount + rejectedCount;

    Double approvalRate = 0.0;
    if (decisionedCount > 0) {
      approvalRate = (double) approvedCount / decisionedCount * 100;
    }

    // 3. Trends (Get Chart Data FIRST to calculate Disbursed Amount from it)
    ChartDataDTO chartData = getChartData(year);

    // 2. Financial Overview
    // Total Disbursed: Sum of monthly stats for consistency with the chart
    Double totalDisbursedAmount = chartData.getMonthlyStats().values().stream().mapToDouble(Double::doubleValue).sum();

    // Financials for this Vintage (Loans originated in this year)
    // Using PAID status for "realized" revenue is tricky if partial payments exist,
    // but here we assume full PAID status
    Double totalPaidAmount = loanApplicationRepository.sumAmountByCurrentStatusAndYear(LoanStatus.PAID.name(), year);
    Double totalPaidTotalAmount = loanApplicationRepository.sumTotalAmountToPayByCurrentStatusAndYear(
        LoanStatus.PAID.name(), year);
    Double totalInterestEarned = (totalPaidTotalAmount != null ? totalPaidTotalAmount : 0)
        - (totalPaidAmount != null ? totalPaidAmount : 0);

    Double outstandingDisbursed = loanApplicationRepository.sumAmountByCurrentStatusAndYear(
        LoanStatus.DISBURSED.name(), year);
    // "Outstanding Principal" usually means principal not yet paid back.
    // If status is DISBURSED, it's all outstanding.
    Double outstandingPrincipal = outstandingDisbursed;

    // Potential Revenue: Interest from active loans (DISBURSED)
    Double disbursedTotalToPay = loanApplicationRepository.sumTotalAmountToPayByCurrentStatusAndYear(
        LoanStatus.DISBURSED.name(), year);
    Double potentialRevenue = (disbursedTotalToPay != null ? disbursedTotalToPay : 0)
        - (outstandingDisbursed != null ? outstandingDisbursed : 0);

    return ExecutiveDashboardDTO.builder()
        .statusCounts(statusCounts)
        .totalApplications(totalApplications)
        .approvalRate(approvalRate)
        .totalDisbursedAmount(totalDisbursedAmount)
        .totalInterestEarned(totalInterestEarned)
        .outstandingPrincipal(outstandingPrincipal)
        .potentialRevenue(potentialRevenue)
        .disbursementTrend(chartData)
        .build();
  }

  private ChartDataDTO getChartData(Integer year) {
    // Yearly Trend
    List<Object[]> yearlyData = loanHistoryRepository.findYearlyDisbursementTrend();
    Map<Integer, Double> yearlyTrend = new TreeMap<>(); // TreeMap for sorted keys
    for (Object[] row : yearlyData) {
      Integer y = (Integer) row[0];
      Double amount = (Double) row[1];
      yearlyTrend.put(y, amount);
    }

    // Monthly Trend for specific year
    List<Object[]> monthlyData = loanHistoryRepository.findMonthlyDisbursementStats(year);
    Map<String, Double> monthlyStats = new HashMap<>(); // Could use generic map or keep order if needed
    // Initialize months to 0
    String[] months = {
        "JANUARY",
        "FEBRUARY",
        "MARCH",
        "APRIL",
        "MAY",
        "JUNE",
        "JULY",
        "AUGUST",
        "SEPTEMBER",
        "OCTOBER",
        "NOVEMBER",
        "DECEMBER"
    };
    for (String month : months) {
      monthlyStats.put(month, 0.0);
    }

    for (Object[] row : monthlyData) {
      Integer m = (Integer) row[0]; // 1-12
      Double amount = (Double) row[1];
      if (m >= 1 && m <= 12) {
        monthlyStats.put(months[m - 1], amount);
      }
    }

    return ChartDataDTO.builder()
        .year(year)
        .yearlyTrend(yearlyTrend)
        .monthlyStats(monthlyStats)
        .build();
  }
}
