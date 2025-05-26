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
}