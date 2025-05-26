package com.wms.warehouse.model;

import jakarta.persistence.*;

@Entity
@Table(name = "warehouse_info")
public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "total_capacity")
    private double totalCapacity;

    @Column(name = "remaining_capacity")
    private double remainingCapacity;

    @Column(name = "total_quantity")
    private Long totalQuantity;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getTotalCapacity() {
        return totalCapacity;
    }

    public void setTotalCapacity(double totalCapacity) {
        this.totalCapacity = totalCapacity;
    }

    public double getRemainingCapacity() {
        return remainingCapacity;
    }

    public void setRemainingCapacity(double remainingCapacity) {
        this.remainingCapacity = remainingCapacity;
    }

    public Long getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(double totalQuantity) {
        this.totalCapacity = totalQuantity;
    }
}