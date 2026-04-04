package com.Zorvyn.Finance_Dashboard.dto;

import com.Zorvyn.Finance_Dashboard.model.RecordType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateFinancialRecordRequest(
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        BigDecimal amount,

        @NotNull(message = "Type is required")
        RecordType type,

        @NotBlank(message = "Category is required")
        String category,

        @NotNull(message = "Date is required")
        LocalDate date,

        String notes
) {
}
