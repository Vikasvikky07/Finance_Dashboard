package com.Zorvyn.Finance_Dashboard.repository;

import com.Zorvyn.Finance_Dashboard.model.FinancialRecord;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, Long>, JpaSpecificationExecutor<FinancialRecord> {

    Optional<FinancialRecord> findByIdAndDeletedFalse(Long id);
}
