package com.Zorvyn.Finance_Dashboard.dto;

import java.math.BigDecimal;

public record TrendPointResponse(
        String period,
        BigDecimal income,
        BigDecimal expense
) {
}
