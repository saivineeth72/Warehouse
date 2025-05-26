package com.wms.warehouse.controller;

import com.wms.warehouse.model.Warehouse;
import com.wms.warehouse.repository.WarehouseRepository;
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
    public Double getRemainingCapacity() {
        return warehouseRepository.findById(1L)
                .map(Warehouse::getRemainingCapacity)
                .orElse(0.0);
    }

    // ✅ NEW: Endpoint to get total quantity in storage
    @GetMapping("/total-quantity")
    public Long getTotalQuantity() {
        return warehouseRepository.findById(1L)
                .map(Warehouse::getTotalQuantity)
                .orElse(0L);
    }
}