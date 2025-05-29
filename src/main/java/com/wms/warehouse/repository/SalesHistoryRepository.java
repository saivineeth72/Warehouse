package com.wms.warehouse.repository;

import com.wms.warehouse.model.SalesHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SalesHistoryRepository extends JpaRepository<SalesHistory, Long> {
    List<SalesHistory> findAllByOrderBySaleDateDesc();
}