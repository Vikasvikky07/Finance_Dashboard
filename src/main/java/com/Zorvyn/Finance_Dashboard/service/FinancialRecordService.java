package com.Zorvyn.Finance_Dashboard.service;

import com.Zorvyn.Finance_Dashboard.dto.CreateFinancialRecordRequest;
import com.Zorvyn.Finance_Dashboard.dto.FinancialRecordResponse;
import com.Zorvyn.Finance_Dashboard.dto.PagedResponse;
import com.Zorvyn.Finance_Dashboard.dto.UpdateFinancialRecordRequest;
import com.Zorvyn.Finance_Dashboard.exception.ResourceNotFoundException;
import com.Zorvyn.Finance_Dashboard.model.AppUser;
import com.Zorvyn.Finance_Dashboard.model.FinancialRecord;
import com.Zorvyn.Finance_Dashboard.model.RecordType;
import com.Zorvyn.Finance_Dashboard.repository.AppUserRepository;
import com.Zorvyn.Finance_Dashboard.repository.FinancialRecordRepository;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FinancialRecordService {

    private final FinancialRecordRepository financialRecordRepository;
    private final AppUserRepository appUserRepository;

    @Transactional(readOnly = true)
    public PagedResponse<FinancialRecordResponse> getRecords(String category, String search, RecordType type,
                                                             LocalDate startDate, LocalDate endDate,
                                                             int page, int size) {
        validateDateRange(startDate, endDate);
        validatePagination(page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<FinancialRecordResponse> result = financialRecordRepository
                .findAll(buildSpecification(category, search, type, startDate, endDate), pageable)
                .map(this::toResponse);

        return new PagedResponse<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isFirst(),
                result.isLast()
        );
    }

    @Transactional(readOnly = true)
    public FinancialRecordResponse getRecord(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional
    public FinancialRecordResponse createRecord(CreateFinancialRecordRequest request) {
        FinancialRecord record = new FinancialRecord();
        applyRequest(record, request.amount(), request.type(), request.category(), request.date(), request.notes());
        record.setCreatedBy(getCurrentUser());
        return toResponse(financialRecordRepository.save(record));
    }

    @Transactional
    public FinancialRecordResponse updateRecord(Long id, UpdateFinancialRecordRequest request) {
        FinancialRecord record = getEntity(id);
        applyRequest(record, request.amount(), request.type(), request.category(), request.date(), request.notes());
        return toResponse(financialRecordRepository.save(record));
    }

    @Transactional
    public void deleteRecord(Long id) {
        FinancialRecord record = getEntity(id);
        record.setDeleted(true);
        financialRecordRepository.save(record);
    }

    private Specification<FinancialRecord> buildSpecification(String category, String search, RecordType type,
                                                              LocalDate startDate, LocalDate endDate) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.isFalse(root.get("deleted")));

            if (category != null && !category.isBlank()) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("category")), category.trim().toLowerCase()));
            }
            if (search != null && !search.isBlank()) {
                String normalized = "%" + search.trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("category")), normalized),
                        criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.coalesce(root.get("notes"), "")), normalized)
                ));
            }
            if (type != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), type));
            }
            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("date"), startDate));
            }
            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("date"), endDate));
            }

            query.orderBy(criteriaBuilder.desc(root.get("date")), criteriaBuilder.desc(root.get("id")));
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate must be before or equal to endDate");
        }
    }

    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("page must be greater than or equal to 0");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("size must be between 1 and 100");
        }
    }

    private FinancialRecord getEntity(Long id) {
        return financialRecordRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Financial record not found with id " + id));
    }

    private void applyRequest(FinancialRecord record, BigDecimal amount, RecordType type,
                              String category, LocalDate date, String notes) {
        record.setAmount(amount);
        record.setType(type);
        record.setCategory(category.trim());
        record.setDate(date);
        record.setNotes(notes == null || notes.isBlank() ? null : notes.trim());
    }

    private AppUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return appUserRepository.findByUsernameIgnoreCaseAndDeletedFalse(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
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
