package com.wms.warehouse.repository;

import com.wms.warehouse.model.DemandHistory;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DemandHistoryRepository extends CrudRepository<DemandHistory, Long> {

    @Query("SELECT EXTRACT(MONTH FROM d.date) AS month, SUM(d.quantity) AS total " +
           "FROM DemandHistory d WHERE d.productCode = :productCode " +
           "AND d.date >= :start AND d.date <= :end " +
           "GROUP BY EXTRACT(YEAR FROM d.date), EXTRACT(MONTH FROM d.date) " +
           "ORDER BY month")
    List<Object[]> findMonthlyDemandByProduct(String productCode, LocalDate start, LocalDate end);
}