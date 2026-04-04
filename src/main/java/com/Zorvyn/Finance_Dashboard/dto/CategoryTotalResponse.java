package com.Zorvyn.Finance_Dashboard.dto;

import java.math.BigDecimal;

public record CategoryTotalResponse(
        String category,
        BigDecimal total
) {
}
