package com.wms.warehouse.repository;

import com.wms.warehouse.model.SalesHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface SalesHistoryRepository extends JpaRepository<SalesHistory, Long> {
    List<SalesHistory> findAllByOrderBySaleDateDesc();

    @Query("SELECT FUNCTION('DATE_TRUNC', 'month', sh.saleDate) as month, SUM(sh.quantitySold) " +
           "FROM SalesHistory sh " +
           "WHERE sh.productName = :productName " +
           "GROUP BY FUNCTION('DATE_TRUNC', 'month', sh.saleDate) " +
           "ORDER BY month")
    List<Object[]> findMonthlySalesByProduct(@Param("productName") String productName);

    @Query("SELECT FUNCTION('DATE_TRUNC', 'month', sh.saleDate) as month, SUM(sh.quantitySold) " +
           "FROM SalesHistory sh " +
           "WHERE sh.productName = :productName AND sh.supplierName = :supplierName " +
           "GROUP BY FUNCTION('DATE_TRUNC', 'month', sh.saleDate) " +
           "ORDER BY month")
    List<Object[]> findMonthlySalesByProductAndSupplier(@Param("productName") String productName, @Param("supplierName") String supplierName);
}