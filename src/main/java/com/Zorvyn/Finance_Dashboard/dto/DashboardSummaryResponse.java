package com.Zorvyn.Finance_Dashboard.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardSummaryResponse(
        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        BigDecimal netBalance,
        List<CategoryTotalResponse> categoryTotals,
        List<FinancialRecordResponse> recentActivity,
        List<TrendPointResponse> monthlyTrends
) {
}
