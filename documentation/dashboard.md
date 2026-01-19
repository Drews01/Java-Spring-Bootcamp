# Dashboard API Documentation

## Overview
The Dashboard API provides real-time analytics and workflow data for different staff roles. It is divided into two main sections:
1.  **Staff Dashboard (`/api/staff/dashboard`)**: Operational workflow data (queues, pending actions).
2.  **Executive Analytics (`/api/staff/dashboard/analytics`)**: High-level metrics, financial health, and trends.

## 1. Executive Analytics API
**Endpoint:** `GET /api/staff/dashboard/analytics`
**Roles:** `ADMIN`, `MARKETING`, `BRANCH_MANAGER`, `BACK_OFFICE`

### Query Parameters
| Parameter | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `year` | `Integer` | No | The year for **ALL** metrics (Counts, Financials, Trends). Defaults to current year. |

### Response Structure (`ExecutiveDashboardDTO`)

> **Note:** All counts and amounts are strictly filtered by the selected year.
> - **Counts**: Based on loan `createdAt` year (Cohort).
> - **Disbursed Amount**: Based on loan aggregation (Sum of monthly disbursements in that year).
> - **Financials**: Based on the vintage of the loan (loans originating in that year).

```json
{
  "statusCounts": {
    "SUBMITTED": 12,
    "WAITING_APPROVAL": 5,
    "APPROVED_WAITING_DISBURSEMENT": 3,
    "REJECTED": 2,
    "DISBURSED": 150
  },
  "totalApplications": 172,
  "approvalRate": 85.5,
  "totalDisbursedAmount": 5000000.0,
  "totalInterestEarned": 120000.0,
  "outstandingPrincipal": 4500000.0,
  "potentialRevenue": 900000.0,
  "disbursementTrend": {
    "year": 2024,
    "monthlyStats": {
      "JANUARY": 50000.0,
      "FEBRUARY": 75000.0,
      ...
    },
    "yearlyTrend": {
      "2023": 1200000.0,
      "2024": 3500000.0
    }
  }
}
```

### Key Metrics Definition
- **Approval Rate**: `Approved Loans / (Approved + Rejected Loans) * 100`
- **Outstanding Principal**: Total amount of loans currently in `DISBURSED` status (Principal only).
- **Potential Revenue**: Expected interest earnings from currently active (`DISBURSED`) loans (`TotalAmountToPay - Principal`).
- **Total Interest Earned**: Realized profit from `PAID` loans.

---

## 2. Staff Workflow Dashboard
**Endpoint:** `GET /api/staff/dashboard`
**Roles:** `MARKETING`, `BRANCH_MANAGER`, `BACK_OFFICE`

Returns the user's role, their specific queue name, and allowed actions.

---

## implementation Details

### DTOs
- `ExecutiveDashboardDTO`: Main response object.
- `ChartDataDTO`: Holds nested map structures for charts.

### Service Layer
- `DashboardService`: Orchestrates data fetching.
    - Uses `LoanApplicationRepository` for real-time counts and sums.
    - Uses `LoanHistoryRepository` for time-series data (charts).

### Performance Note
- Trend queries use database-level aggregation (`GROUP BY`) to ensure performance even with large datasets.
- Counts are optimized using indexed status columns.
