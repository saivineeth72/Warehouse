package com.wms.warehouse.model;

import java.text.NumberFormat;
import java.util.Locale;

public class ProductDemand {
    private Long productId;
    private String productName;
    private String brand;
    private String supplierName;
    private String forecastedDemand;   // Changed to String
    private String currentQuantity;    // Changed to String

    public ProductDemand() {
    }

    public ProductDemand(Long productId, String productName, String brand, String supplierName, int forecastedDemand, int currentQuantity) {
        this.productId = productId;
        this.productName = productName;
        this.brand = brand;
        this.supplierName = supplierName;
        this.forecastedDemand = formatNumber(forecastedDemand);
        this.currentQuantity = formatNumber(currentQuantity);
    }

    private String formatNumber(int value) {
        return NumberFormat.getNumberInstance(Locale.US).format(value);
    }

    // Getters
    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public String getForecastedDemand() { return forecastedDemand; }
    public String getCurrentQuantity() { return currentQuantity; }
    public String getBrand() { return brand; }
    public String getSupplierName() { return supplierName; }

    // Setters
    public void setProductId(Long productId) { this.productId = productId; }
    public void setProductName(String productName) { this.productName = productName; }
    public void setForecastedDemand(String forecastedDemand) { this.forecastedDemand = forecastedDemand; }
    public void setCurrentQuantity(String currentQuantity) { this.currentQuantity = currentQuantity; }
    public void setBrand(String brand) { this.brand = brand; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
}