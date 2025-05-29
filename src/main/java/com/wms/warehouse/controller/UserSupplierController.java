package com.wms.warehouse.controller;

import com.wms.warehouse.model.User;
import com.wms.warehouse.model.Supplier;
import com.wms.warehouse.repository.UserRepository;
import com.wms.warehouse.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@CrossOrigin
public class UserSupplierController {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @GetMapping("/{username}")
    public ResponseEntity<Supplier> getUser(@PathVariable String username) {
        User user = userRepository.findByUsername(username);
        Supplier supplier = supplierRepository.findById(user.getSupplierId()).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(supplier);
    }
}