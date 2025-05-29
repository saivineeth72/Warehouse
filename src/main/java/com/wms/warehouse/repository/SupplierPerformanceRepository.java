package com.wms.warehouse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.wms.warehouse.model.SupplierPerformance;
import java.util.List;

public interface SupplierPerformanceRepository extends JpaRepository<SupplierPerformance, Long> {
    List<SupplierPerformance> findAllByOrderDateNotNullAndReceivedDateNotNull();
}

