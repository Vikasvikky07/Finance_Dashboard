package com.Zorvyn.Finance_Dashboard.controller;

import com.Zorvyn.Finance_Dashboard.dto.CreateFinancialRecordRequest;
import com.Zorvyn.Finance_Dashboard.dto.FinancialRecordResponse;
import com.Zorvyn.Finance_Dashboard.dto.PagedResponse;
import com.Zorvyn.Finance_Dashboard.dto.UpdateFinancialRecordRequest;
import com.Zorvyn.Finance_Dashboard.model.RecordType;
import com.Zorvyn.Finance_Dashboard.service.FinancialRecordService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class FinancialRecordController {

    private final FinancialRecordService financialRecordService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public PagedResponse<FinancialRecordResponse> getRecords(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) RecordType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return financialRecordService.getRecords(category, search, type, startDate, endDate, page, size);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public FinancialRecordResponse getRecord(@PathVariable Long id) {
        return financialRecordService.getRecord(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public FinancialRecordResponse createRecord(@Valid @RequestBody CreateFinancialRecordRequest request) {
        return financialRecordService.createRecord(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public FinancialRecordResponse updateRecord(@PathVariable Long id,
                                                @Valid @RequestBody UpdateFinancialRecordRequest request) {
        return financialRecordService.updateRecord(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteRecord(@PathVariable Long id) {
        financialRecordService.deleteRecord(id);
    }
}
