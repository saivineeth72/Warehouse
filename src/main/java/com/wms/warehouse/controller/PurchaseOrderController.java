package com.wms.warehouse.controller;

import com.wms.warehouse.model.PurchaseOrder;
import com.wms.warehouse.model.Product;
import com.wms.warehouse.model.Supplier;
import com.wms.warehouse.repository.PurchaseOrderRepository;
import com.wms.warehouse.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@CrossOrigin(origins = "*")
public class PurchaseOrderController {
    @Autowired
    private PurchaseOrderRepository orderRepo;
    @Autowired
    private ProductRepository productRepo;

    @PostMapping("/auto")
    public ResponseEntity<?> autoOrder(@RequestParam Long productId, @RequestParam int quantity) {
        Product product = productRepo.findById(productId).orElse(null);
        if (product == null) return ResponseEntity.badRequest().body("Product not found");
        Supplier supplier = product.getSupplier();
        if (supplier == null) return ResponseEntity.badRequest().body("Supplier not found for product");
        if (quantity <= 0) return ResponseEntity.badRequest().body("Quantity must be positive");

        PurchaseOrder order = new PurchaseOrder();
        order.setProduct(product);
        order.setSupplier(supplier);
        order.setQuantity(quantity);
        order.setStatus("CREATED");
        orderRepo.save(order);
        product.setQuantity(product.getQuantity() + quantity);
        productRepo.save(product);
        return ResponseEntity.ok(order);
    }
} 