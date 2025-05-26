package com.wms.warehouse.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "product_demand_history")
public class DemandHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_code")
    private String productCode;

    @Column(name = "demand_date")
    private LocalDate date;

    private int quantity;

    // Getters and Setters
    public Long getId() { return id; }
    public String getProductCode() { return productCode; }
    public LocalDate getDate() { return date; }
    public int getQuantity() { return quantity; }

    public void setId(Long id) { this.id = id; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}