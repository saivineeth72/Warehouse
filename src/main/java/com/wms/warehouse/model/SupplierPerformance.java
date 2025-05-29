package com.wms.warehouse.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "supplier_performance_history")
public class SupplierPerformance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "order_date")
    private LocalDate orderDate;

    @Column(name = "received_date")
    private LocalDate receivedDate;

    @Column(name = "order_demand")
    private Integer orderDemand;

    @ManyToOne
    @JoinColumn(name = "supplier", referencedColumnName = "name")
    private Supplier supplier;

    // Constructors
    public SupplierPerformance() {}

    public SupplierPerformance(String productName, LocalDate orderDate, LocalDate receivedDate, Supplier supplier, Integer orderDemand) {
        this.productName = productName;
        this.orderDate = orderDate;
        this.receivedDate = receivedDate;
        this.supplier = supplier;
        this.orderDemand = orderDemand;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }

    public LocalDate getReceivedDate() { return receivedDate; }
    public void setReceivedDate(LocalDate receivedDate) { this.receivedDate = receivedDate; }

    public Integer getOrderDemand() { return orderDemand; }
    public void setOrderDemand(Integer orderDemand) { this.orderDemand = orderDemand; }

    public Supplier getSupplier() { return supplier; }
    public void setSupplier(Supplier supplier) { this.supplier = supplier; }
}