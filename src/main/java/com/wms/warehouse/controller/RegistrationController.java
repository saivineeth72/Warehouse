package com.wms.warehouse.controller;

import com.wms.warehouse.model.Supplier;
import com.wms.warehouse.model.User;
import com.wms.warehouse.repository.SupplierRepository;
import com.wms.warehouse.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class RegistrationController {

    @Autowired
    private SupplierRepository supplierRepo;

    @Autowired
    private UserRepository userRepo;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @PostMapping("/register-supplier")
    public String registerSupplier(@RequestBody SupplierRegistrationDTO dto) {
        // Print received data
        System.out.println("\n=== Received Supplier Registration Data ===");
        System.out.println(dto.toString());

        Supplier supplier = new Supplier();
        supplier.setName(dto.getName());
        supplier.setContactInfo(dto.getContactInfo());
        supplier.setEmail(dto.getEmail());
        supplier.setPhone(dto.getPhone());
        Supplier savedSupplier = supplierRepo.save(supplier);

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(encoder.encode(dto.getPassword()));
        user.setRole("supplier");
        user.setSupplierId(savedSupplier.getId());
        userRepo.save(user);

        return "Supplier registered successfully";
    }

    // 🔽 Inline DTO class
    public static class SupplierRegistrationDTO {
        private String name;           // changed from companyName to match frontend
        private String contactInfo;
        private String email;
        private String phone;
        private String username;
        private String password;

        @Override
        public String toString() {
            return String.format("""
                SupplierRegistrationDTO {
                    name: '%s',
                    contactInfo: '%s',
                    email: '%s',
                    phone: '%s',
                    username: '%s',
                    password: '[HIDDEN]'
                }""", 
                name,
                contactInfo,
                email,
                phone,
                username
            );
        }

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getContactInfo() { return contactInfo; }
        public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}