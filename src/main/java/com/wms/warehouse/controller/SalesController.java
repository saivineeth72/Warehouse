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
        return productRepo.findByNameContainingIgnoreCase(keyword);
    }

    // === BACKEND CHANGES NEEDED ===
// Replace your existing purchaseProduct() method with this updated one to return detailed breakdown:

    @PostMapping("/purchase")
    public ResponseEntity<?> purchaseProductDetailed(@RequestParam Long productId,
                                                    @RequestParam int quantityRequested) {
        Optional<Product> productOpt = productRepo.findById(productId);
        if (productOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Product not found"));
        }
        Product product = productOpt.get();
        if (product.getQuantity() < quantityRequested) {
            return ResponseEntity.badRequest().body(Map.of("error", "Not enough stock. Available: " + product.getQuantity()));
        }
        product.setQuantity(product.getQuantity() - quantityRequested);
        productRepo.save(product);

        // Add to sales history as before
        SalesHistory history = new SalesHistory();
        history.setProductName(product.getName());
        history.setQuantitySold(quantityRequested);
        history.setUnitPrice(product.getValue().doubleValue());
        history.setTotalPrice(product.getValue().doubleValue() * quantityRequested);
        history.setSupplierName(product.getSupplier().getName());
        history.setBrand(product.getBrand());
        salesHistoryRepo.save(history);

        return ResponseEntity.ok(Map.of(
            "product", product.getName(),
            "supplier", product.getSupplier().getName(),
            "location", product.getLocation(),
            "quantity", quantityRequested,
            "unitValue", product.getValue(),
            "total", product.getValue().doubleValue() * quantityRequested
        ));
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
