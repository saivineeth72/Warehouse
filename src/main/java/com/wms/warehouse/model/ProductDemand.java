package com.wms.warehouse.model;

import java.text.NumberFormat;
import java.util.Locale;

public class ProductDemand {
    private Long productId;
    private String productName;
    private String forecastedDemand;   // Changed to String
    private String currentQuantity;    // Changed to String

    public ProductDemand() {
    }

    public ProductDemand(Long productId, String productName, int forecastedDemand, int currentQuantity) {
        this.productId = productId;
        this.productName = productName;
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

    // Setters
    public void setProductId(Long productId) { this.productId = productId; }
    public void setProductName(String productName) { this.productName = productName; }
    public void setForecastedDemand(String forecastedDemand) { this.forecastedDemand = forecastedDemand; }
    public void setCurrentQuantity(String currentQuantity) { this.currentQuantity = currentQuantity; }
}