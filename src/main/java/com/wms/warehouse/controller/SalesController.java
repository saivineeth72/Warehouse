package com.wms.warehouse.controller;

import com.wms.warehouse.model.Product;
import com.wms.warehouse.repository.ProductRepository;
import com.wms.warehouse.repository.SalesHistoryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.wms.warehouse.model.SalesHistory;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/sales")
public class SalesController {

    private final ProductRepository productRepo;
    private final SalesHistoryRepository salesHistoryRepo;

    public SalesController(ProductRepository productRepo, SalesHistoryRepository salesHistoryRepo) {
        this.productRepo = productRepo;
        this.salesHistoryRepo = salesHistoryRepo;
    }

    @GetMapping("/search")
    public List<Product> searchProducts(@RequestParam String keyword) {
        return productRepo.findByNameContainingIgnoreCase(keyword).stream()
                .collect(Collectors.groupingBy(Product::getName))
                .entrySet().stream()
                .map(entry -> {
                    List<Product> group = entry.getValue();
                    Product base = new Product();
                    Product first = group.get(0);  // Get first product for required fields
                    
                    base.setName(entry.getKey());
                    base.setQuantity(group.stream().mapToInt(Product::getQuantity).sum());
                    base.setBrand(first.getBrand());
                    base.setSizeSqm(first.getSizeSqm());
                    base.setValue(first.getValue());
                    base.setSupplier(first.getSupplier());
                    base.setReorderLevel(first.getReorderLevel());
                    base.setCategory(first.getCategory());
                    base.setLocation(first.getLocation());
                    base.setSku(first.getSku());
                    
                    return base;
                })
                .collect(Collectors.toList());
    }

    // === BACKEND CHANGES NEEDED ===
// Replace your existing purchaseProduct() method with this updated one to return detailed breakdown:

    @PostMapping("/purchase")
    public ResponseEntity<?> purchaseProductDetailed(@RequestParam String productName,
                                                    @RequestParam int quantityRequested) {
        List<Product> matchingProducts = productRepo.findByNameContainingIgnoreCase(productName)
                .stream()
                .sorted(Comparator.comparing(Product::getValue))
                .collect(Collectors.toList());

        int totalAvailable = matchingProducts.stream()
                .mapToInt(Product::getQuantity)
                .sum();

        if (totalAvailable < quantityRequested) {
            return ResponseEntity.badRequest().body(Map.of("error", "Not enough stock. Available: " + totalAvailable));
        }

        int remaining = quantityRequested;
        List<Product> updatedProducts = new ArrayList<>();
        List<Long> toDelete = new ArrayList<>();
        List<Map<String, Object>> breakdown = new ArrayList<>();

        for (Product p : matchingProducts) {
            if (remaining == 0) break;
            int available = p.getQuantity();

            int used = Math.min(available, remaining);
            double unitValue = p.getValue().doubleValue();

            breakdown.add(Map.of(
                "supplier", p.getSupplier(),
                "location", p.getLocation(),
                "quantity", used,
                "unitValue", unitValue,
                "total", unitValue * used
            ));

            if (available <= remaining) {
                remaining -= available;
                toDelete.add(p.getId());
            } else {
                p.setQuantity(available - remaining);
                updatedProducts.add(p);
                remaining = 0;
            }
        }

        updatedProducts.forEach(productRepo::save);
        toDelete.forEach(productRepo::deleteById);

        for (Map<String, Object> row : breakdown) {
            SalesHistory history = new SalesHistory();
            history.setProductName(productName);
            history.setQuantitySold((Integer) row.get("quantity"));
            history.setUnitPrice(((Number) row.get("unitValue")).doubleValue());
            history.setTotalPrice(((Number) row.get("total")).doubleValue());
            history.setSupplierName(row.get("supplier").toString());
            salesHistoryRepo.save(history);
        }

        return ResponseEntity.ok(breakdown);
    }

    @GetMapping("/available")
    public ResponseEntity<?> getAvailableQuantity(@RequestParam String productName) {
        List<Product> matches = productRepo.findByNameContainingIgnoreCase(productName);
        int total = matches.stream().mapToInt(Product::getQuantity).sum();
        return ResponseEntity.ok(Map.of("totalAvailable", total));
    }

    @GetMapping("/history")
    public List<SalesHistory> getSalesHistory() {
        return salesHistoryRepo.findAllByOrderBySaleDateDesc();
    }
}
