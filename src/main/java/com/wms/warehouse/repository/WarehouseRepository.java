package com.wms.warehouse.repository;

import com.wms.warehouse.model.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    @Query("SELECT w.remainingCapacity FROM Warehouse w WHERE w.id = 1")
    double getRemainingCapacity();

    @Query("SELECT w.totalQuantity FROM Warehouse w WHERE w.id = 1")
    long getTotalQuantity();
}