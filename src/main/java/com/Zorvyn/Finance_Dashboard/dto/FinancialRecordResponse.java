package com.Zorvyn.Finance_Dashboard.dto;

import com.Zorvyn.Finance_Dashboard.model.RecordType;
import java.math.BigDecimal;
import java.time.LocalDate;

public record FinancialRecordResponse(
        Long id,
        BigDecimal amount,
        RecordType type,
        String category,
        LocalDate date,
        String notes,
        String createdBy
) {
}
