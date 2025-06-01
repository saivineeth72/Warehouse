package com.wms.warehouse.controller;

import com.wms.warehouse.model.Product;
import com.wms.warehouse.model.ProductDemand;
import com.wms.warehouse.repository.ProductRepository;
import com.wms.warehouse.service.SalesService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;            // ← only needed if you add custom headers
import org.springframework.http.ResponseEntity;      // ← only needed if you return ResponseEntity
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/forecast")
@CrossOrigin(origins = "*")
public class ForecastWarningController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SalesService salesService;

    @GetMapping("/warnings")
    public List<ProductDemand> getWarnings() {
        // Group by name, brand, supplier
        List<Product> products = productRepository.findAll();
        List<ProductDemand> results = new ArrayList<>();
        for (Product p : products) {
            String productName = p.getName();
            String brand = p.getBrand();
            String supplierName = p.getSupplier().getName();
            int totalQuantity = p.getQuantity();
            int forecast = salesService.predictNextMonthDemand(productName, brand, supplierName);
            results.add(new ProductDemand(p.getId(), productName, brand, supplierName, forecast, totalQuantity));
        }
        return results;
    }

}