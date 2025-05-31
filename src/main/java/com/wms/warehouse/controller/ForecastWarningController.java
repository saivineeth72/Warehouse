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
        // Current stock is from the product table
        // Forecasted demand is now based on sales history (not demand history)
        List<Product> products = productRepository.findAll();
        Map<String, Integer> productStockMap = new HashMap<>();
        Map<String, Long> productIdMap = new HashMap<>(); // Track productId for each name

        // Group products by name and sum their quantities
        for (Product p : products) {
            productStockMap.put(
                p.getName(),
                productStockMap.getOrDefault(p.getName(), 0) + p.getQuantity()
            );
            productIdMap.putIfAbsent(p.getName(), p.getId()); // Save first productId for each name
        }

        List<ProductDemand> results = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : productStockMap.entrySet()) {
            String productName = entry.getKey();
            int totalQuantity = entry.getValue();
            int forecast = salesService.predictNextMonthDemand(productName); // Now uses sales history

            Long productId = productIdMap.get(productName);
            results.add(new ProductDemand(productId, productName, forecast, totalQuantity));
        }

        return results;
    }

}