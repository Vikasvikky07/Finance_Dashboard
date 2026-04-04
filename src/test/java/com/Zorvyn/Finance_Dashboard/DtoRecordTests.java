package com.Zorvyn.Finance_Dashboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.Zorvyn.Finance_Dashboard.dto.ApiErrorResponse;
import com.Zorvyn.Finance_Dashboard.dto.AuthRequest;
import com.Zorvyn.Finance_Dashboard.dto.AuthResponse;
import com.Zorvyn.Finance_Dashboard.dto.CategoryTotalResponse;
import com.Zorvyn.Finance_Dashboard.dto.CreateFinancialRecordRequest;
import com.Zorvyn.Finance_Dashboard.dto.CreateUserRequest;
import com.Zorvyn.Finance_Dashboard.dto.DashboardSummaryResponse;
import com.Zorvyn.Finance_Dashboard.dto.FinancialRecordResponse;
import com.Zorvyn.Finance_Dashboard.dto.TrendPointResponse;
import com.Zorvyn.Finance_Dashboard.dto.UpdateFinancialRecordRequest;
import com.Zorvyn.Finance_Dashboard.dto.UpdateUserPasswordRequest;
import com.Zorvyn.Finance_Dashboard.dto.UpdateUserRequest;
import com.Zorvyn.Finance_Dashboard.dto.UserResponse;
import com.Zorvyn.Finance_Dashboard.model.RecordType;
import com.Zorvyn.Finance_Dashboard.model.UserStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class DtoRecordTests {

    @Test
    void authDtosExposeExpectedValues() {
        AuthRequest request = new AuthRequest("admin", "password");
        UserResponse user = new UserResponse(1L, "Admin User", "admin", "admin@finance.local", Set.of("ADMIN", "ANALYST"), UserStatus.ACTIVE);
        AuthResponse response = new AuthResponse("token-value", "Bearer", 3600L, user);

        assertEquals("admin", request.username());
        assertEquals("password", request.password());
        assertEquals("token-value", response.accessToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals(3600L, response.expiresIn());
        assertEquals("admin", response.user().username());
        assertIterableEquals(Set.of("ADMIN", "ANALYST"), response.user().roles());
    }

    @Test
    void financialRecordDtosExposeExpectedValues() {
        LocalDate date = LocalDate.of(2026, 4, 4);
        BigDecimal amount = new BigDecimal("95.25");

        CreateFinancialRecordRequest createRequest = new CreateFinancialRecordRequest(amount, RecordType.EXPENSE, "Internet", date, "Monthly broadband bill");
        UpdateFinancialRecordRequest updateRequest = new UpdateFinancialRecordRequest(amount, RecordType.EXPENSE, "Internet", date, "Updated note");
        FinancialRecordResponse response = new FinancialRecordResponse(10L, amount, RecordType.EXPENSE, "Internet", date, "Monthly broadband bill", "Admin User");

        assertEquals(amount, createRequest.amount());
        assertEquals(RecordType.EXPENSE, createRequest.type());
        assertEquals("Updated note", updateRequest.notes());
        assertEquals(10L, response.id());
        assertEquals("Admin User", response.createdBy());
    }

    @Test
    void userDtosExposeExpectedValues() {
        CreateUserRequest createRequest = new CreateUserRequest(
                "Finance Manager",
                "manager",
                "manager@finance.local",
                "strongPass123",
                Set.of("VIEWER", "ANALYST"),
                UserStatus.ACTIVE
        );
        UpdateUserRequest updateRequest = new UpdateUserRequest(
                "Finance Manager Updated",
                "manager",
                "manager@finance.local",
                Set.of("ADMIN"),
                UserStatus.INACTIVE
        );
        UpdateUserPasswordRequest passwordRequest = new UpdateUserPasswordRequest("updatedPass123");

        assertEquals("manager", createRequest.username());
        assertIterableEquals(Set.of("VIEWER", "ANALYST"), createRequest.roles());
        assertIterableEquals(Set.of("ADMIN"), updateRequest.roles());
        assertEquals(UserStatus.INACTIVE, updateRequest.status());
        assertEquals("updatedPass123", passwordRequest.password());
    }

    @Test
    void dashboardAndErrorDtosExposeExpectedValues() {
        CategoryTotalResponse categoryTotal = new CategoryTotalResponse("Salary", new BigDecimal("7500.00"));
        TrendPointResponse trendPoint = new TrendPointResponse("2026-04", new BigDecimal("7750.00"), new BigDecimal("2220.50"));
        FinancialRecordResponse recentRecord = new FinancialRecordResponse(
                1L,
                new BigDecimal("7500.00"),
                RecordType.INCOME,
                "Salary",
                LocalDate.of(2026, 4, 1),
                null,
                "Admin User"
        );
        DashboardSummaryResponse dashboard = new DashboardSummaryResponse(
                new BigDecimal("8050.00"),
                new BigDecimal("2496.24"),
                new BigDecimal("5553.76"),
                List.of(categoryTotal),
                List.of(recentRecord),
                List.of(trendPoint)
        );
        ApiErrorResponse error = new ApiErrorResponse(
                Instant.parse("2026-04-04T00:00:00Z"),
                400,
                "Bad Request",
                "Validation failed",
                List.of("Amount is required")
        );

        assertEquals("Salary", categoryTotal.category());
        assertEquals("2026-04", trendPoint.period());
        assertEquals(new BigDecimal("5553.76"), dashboard.netBalance());
        assertNull(dashboard.recentActivity().get(0).notes());
        assertEquals(400, error.status());
        assertEquals("Validation failed", error.message());
    }
}
