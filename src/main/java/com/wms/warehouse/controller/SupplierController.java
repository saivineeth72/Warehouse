package com.wms.warehouse.controller;

import com.wms.warehouse.model.Supplier;
import com.wms.warehouse.repository.SupplierRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/suppliers")
public class SupplierController {

    private final SupplierRepository repo;

    public SupplierController(SupplierRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Supplier> getAll() {
        return repo.findAll();
    }

    @PostMapping
    public Supplier addSupplier(@RequestBody Supplier supplier) {
        return repo.save(supplier);
    }

    @PutMapping("/{id}")
    public Supplier updateSupplier(@PathVariable Long id, @RequestBody Supplier updatedSupplier) {
        return repo.findById(id)
                .map(s -> {
                    s.setName(updatedSupplier.getName());
                    s.setEmail(updatedSupplier.getEmail());
                    s.setPhone(updatedSupplier.getPhone());
                    s.setContactInfo(updatedSupplier.getContactInfo());
                    return repo.save(s);
                })
                .orElseThrow(() -> new RuntimeException("Supplier not found"));
    }

    @DeleteMapping("/{id}")
    public void deleteSupplier(@PathVariable Long id) {
        repo.deleteById(id);
    }

    @GetMapping("/{id}")
    public Supplier getSupplierById(@PathVariable Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));
    }
}