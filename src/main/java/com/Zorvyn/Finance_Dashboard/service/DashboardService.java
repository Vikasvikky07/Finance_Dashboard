package com.Zorvyn.Finance_Dashboard.service;

import com.Zorvyn.Finance_Dashboard.dto.CategoryTotalResponse;
import com.Zorvyn.Finance_Dashboard.dto.DashboardSummaryResponse;
import com.Zorvyn.Finance_Dashboard.dto.FinancialRecordResponse;
import com.Zorvyn.Finance_Dashboard.dto.TrendPointResponse;
import com.Zorvyn.Finance_Dashboard.model.FinancialRecord;
import com.Zorvyn.Finance_Dashboard.model.RecordType;
import com.Zorvyn.Finance_Dashboard.repository.FinancialRecordRepository;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final FinancialRecordRepository financialRecordRepository;

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        List<FinancialRecord> allRecords = financialRecordRepository.findAll(Sort.by(Sort.Direction.DESC, "date", "id"));
        BigDecimal totalIncome = sumByType(allRecords, RecordType.INCOME);
        BigDecimal totalExpenses = sumByType(allRecords, RecordType.EXPENSE);

        return new DashboardSummaryResponse(
                totalIncome,
                totalExpenses,
                totalIncome.subtract(totalExpenses),
                buildCategoryTotals(allRecords),
                allRecords.stream().limit(5).map(this::toResponse).toList(),
                buildMonthlyTrends(allRecords)
        );
    }

    private BigDecimal sumByType(List<FinancialRecord> records, RecordType type) {
        return records.stream()
                .filter(record -> record.getType() == type)
                .map(FinancialRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<CategoryTotalResponse> buildCategoryTotals(List<FinancialRecord> records) {
        Map<String, BigDecimal> totals = new LinkedHashMap<>();
        records.forEach(record -> totals.merge(record.getCategory(), record.getAmount(), BigDecimal::add));

        return totals.entrySet()
                .stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .map(entry -> new CategoryTotalResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

    private List<TrendPointResponse> buildMonthlyTrends(List<FinancialRecord> records) {
        Map<YearMonth, List<FinancialRecord>> grouped = new LinkedHashMap<>();
        YearMonth currentMonth = YearMonth.now();

        for (int index = 5; index >= 0; index--) {
            grouped.put(currentMonth.minusMonths(index), new ArrayList<>());
        }

        records.forEach(record -> {
            YearMonth month = YearMonth.from(record.getDate());
            if (grouped.containsKey(month)) {
                grouped.get(month).add(record);
            }
        });

        return grouped.entrySet().stream()
                .map(entry -> new TrendPointResponse(
                        entry.getKey().toString(),
                        sumByType(entry.getValue(), RecordType.INCOME),
                        sumByType(entry.getValue(), RecordType.EXPENSE)))
                .toList();
    }

    private FinancialRecordResponse toResponse(FinancialRecord record) {
        return new FinancialRecordResponse(
                record.getId(),
                record.getAmount(),
                record.getType(),
                record.getCategory(),
                record.getDate(),
                record.getNotes(),
                record.getCreatedBy().getName()
        );
    }
}
