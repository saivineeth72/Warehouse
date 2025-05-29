package com.wms.warehouse.service;

import com.wms.warehouse.model.SupplierPerformance;
import com.wms.warehouse.repository.SupplierPerformanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SupplierService {

    @Autowired
    private SupplierPerformanceRepository supplierPerformanceRepository;

    // Existing method: Top 10 fastest suppliers
    public List<Map.Entry<String, Double>> getTop10FastestSuppliers() {
        List<SupplierPerformance> orders = supplierPerformanceRepository.findAllByOrderDateNotNullAndReceivedDateNotNull();

        return orders.stream()
            .collect(Collectors.groupingBy(
                o -> o.getSupplier().getName(),
                Collectors.averagingLong(o -> ChronoUnit.DAYS.between(o.getOrderDate(), o.getReceivedDate()))
            ))
            .entrySet()
            .stream()
            .sorted(Map.Entry.comparingByValue()) // ascending by average days
            .limit(10)
            .toList();
    }

    // ✅ New method: Top 10 most sold products
    public List<Map.Entry<String, Integer>> getTop10MostSoldProducts() {
        List<SupplierPerformance> orders = supplierPerformanceRepository.findAll();

        return orders.stream()
            .collect(Collectors.groupingBy(
                SupplierPerformance::getProductName,
                Collectors.summingInt(SupplierPerformance::getOrderDemand)
            ))
            .entrySet()
            .stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()) // descending by demand
            .limit(10)
            .toList();
    }
}