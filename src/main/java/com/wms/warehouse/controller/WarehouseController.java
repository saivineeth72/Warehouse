package com.wms.warehouse.controller;

import com.wms.warehouse.model.Warehouse;
import com.wms.warehouse.repository.WarehouseRepository;

import java.math.BigInteger;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/warehouse")
public class WarehouseController {

    private final WarehouseRepository warehouseRepository;

    public WarehouseController(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    // Endpoint to get remaining capacity
    @GetMapping("/remaining-capacity")
    public Long getRemainingCapacity() {
        return warehouseRepository.findById(1L)
                .map(Warehouse::getRemainingCapacity)
                .orElse(0L);
    }

    // ✅ NEW: Endpoint to get total quantity in storage
    @GetMapping("/total-quantity")
    public Long getTotalQuantity() {
        return warehouseRepository.findById(1L)
                .map(Warehouse::getTotalQuantity)
                .orElse(0L);
    }
}