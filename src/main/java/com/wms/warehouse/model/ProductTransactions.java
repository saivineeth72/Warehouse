package com.wms.warehouse.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_transactions")
public class ProductTransactions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "action_type", nullable = false)
    private String actionType;

    @Column(name = "old_data", columnDefinition = "jsonb")
    private String oldData;

    @Column(name = "new_data", columnDefinition = "jsonb")
    private String newData;

    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    @Column(name = "category")
    private String category;

    @Column(name = "value", precision = 38, scale = 2, nullable = false)
    private BigDecimal value;

    @Column(name = "timestamp", nullable = false, updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime timestamp;

    // === Constructors ===

    public ProductTransactions() {}

    public ProductTransactions(Long productId, String actionType, String oldData, String newData,
                               Long supplierId, String category, BigDecimal value) {
        this.productId = productId;
        this.actionType = actionType;
        this.oldData = oldData;
        this.newData = newData;
        this.supplierId = supplierId;
        this.category = category;
        this.value = value;
        this.timestamp = LocalDateTime.now();
    }

    // === Getters and Setters ===

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public String getActionType() {
        return actionType;
    }

    public String getOldData() {
        return oldData;
    }

    public String getNewData() {
        return newData;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getValue() {
        return value;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public void setOldData(String oldData) {
        this.oldData = oldData;
    }

    public void setNewData(String newData) {
        this.newData = newData;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}