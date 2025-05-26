package com.wms.warehouse.controller;

import com.wms.warehouse.model.Product;
import com.wms.warehouse.model.Supplier;
import com.wms.warehouse.model.Warehouse;
import com.wms.warehouse.repository.ProductRepository;
import com.wms.warehouse.repository.SupplierRepository;
import com.wms.warehouse.repository.WarehouseRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductRepository repo;
    private final SupplierRepository supplierRepo;
    private final WarehouseRepository warehouseRepo;

    public ProductController(ProductRepository repo, SupplierRepository supplierRepo, WarehouseRepository warehouseRepo) {
        this.repo = repo;
        this.supplierRepo = supplierRepo;
        this.warehouseRepo = warehouseRepo;
    }

    @GetMapping
    public List<Product> getAll() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public Product getOne(@PathVariable Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Product product) {
        // Check if product with same name and brand already exists
        List<Product> duplicates = repo.findByNameAndBrand(product.getName(), product.getBrand());

        if (!duplicates.isEmpty()) {
            Product existing = duplicates.get(0);

            int totalQty = existing.getQuantity() + product.getQuantity();
            double totalSize = (existing.getSizeSqm() * existing.getQuantity()) + (product.getSizeSqm() * product.getQuantity());
            double avgSize = totalSize / totalQty;

            existing.setQuantity(totalQty);
            existing.setSizeSqm(avgSize);
            existing.setValue(product.getValue());
            existing.setLocation(product.getLocation());
            existing.setCategory(product.getCategory());
            existing.setReorderLevel(product.getReorderLevel());

            if (product.getSupplier() != null && product.getSupplier().getId() != null) {
                Supplier supplier = supplierRepo.findById(product.getSupplier().getId())
                        .orElseThrow(() -> new RuntimeException("Supplier not found"));
                existing.setSupplier(supplier);
            }

            Product updated = repo.save(existing);
            return ResponseEntity.ok(updated);
        }

        // Space validation
        double remaining = warehouseRepo.findById(1L)
                .map(Warehouse::getRemainingCapacity)
                .orElse(0.0);

        if (product.getSizeSqm() * product.getQuantity() > remaining) {
            return ResponseEntity.badRequest().body("Not enough warehouse space to add this product. Remaining space: " + remaining);
        }

        Product saved = repo.save(product);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public Product update(@PathVariable Long id, @RequestBody Product updatedProduct) {
        return repo.findById(id)
                .map(product -> {
                    product.setName(updatedProduct.getName());
                    product.setQuantity(updatedProduct.getQuantity());
                    product.setBrand(updatedProduct.getBrand());
                    product.setCategory(updatedProduct.getCategory());
                    product.setLocation(updatedProduct.getLocation());
                    product.setSizeSqm(updatedProduct.getSizeSqm());
                    product.setValue(updatedProduct.getValue());
                    product.setReorderLevel(updatedProduct.getReorderLevel());

                    if (updatedProduct.getSupplier() != null && updatedProduct.getSupplier().getId() != null) {
                        Supplier supplier = supplierRepo.findById(updatedProduct.getSupplier().getId())
                                .orElseThrow(() -> new RuntimeException("Supplier not found"));
                        product.setSupplier(supplier);
                    }

                    return repo.save(product);
                })
                .orElseThrow(() -> new RuntimeException("Product not found with id " + id));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repo.deleteById(id);
    }

    @GetMapping("/search")
    public List<Product> searchByName(@RequestParam String name) {
        return repo.findByNameContainingIgnoreCase(name);
    }

    @GetMapping("/low-stock")
    public List<Product> getLowStockProducts(@RequestParam int threshold) {
        return repo.findByQuantityLessThan(threshold);
    }
}