package com.wms.warehouse.controller;

import com.wms.warehouse.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/supplier-performance")
public class SupplierPerformanceController {

    @Autowired
    private SupplierService supplierService;

    // Endpoint for top 10 fastest suppliers
    @GetMapping("/top-fastest")
    public List<Map.Entry<String, Double>> getTop10FastestSuppliers() {
        return supplierService.getTop10FastestSuppliers();
    }

    // ✅ New endpoint for top 10 most sold products
    @GetMapping("/top-products")
    public List<Map.Entry<String, Integer>> getTop10MostSoldProducts() {
        return supplierService.getTop10MostSoldProducts();
    }
}