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
        List<Product> products = productRepository.findAll();
        List<ProductDemand> warnings = new ArrayList<>();

        for (Product p : products) {
            List<Integer> history = salesService.getMonthlySales(p.getName());
            int forecast = predictNextMonthDemand(history);

            if (forecast > p.getQuantity()) {
                warnings.add(new ProductDemand(p.getId(), p.getName(), forecast, p.getQuantity()));
            }
        }

        return warnings;
    }

    private int predictNextMonthDemand(List<Integer> monthlySales) {
        if (monthlySales == null || monthlySales.isEmpty()) return 0;
        return (int) monthlySales.stream().mapToInt(Integer::intValue).average().orElse(0);
    }
}