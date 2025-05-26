package com.wms.warehouse.gui;

import com.wms.warehouse.model.Product;
import com.wms.warehouse.model.Supplier;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class ProductDetailsWindow {

    private final Product product;

    public ProductDetailsWindow(Product product) {
        this.product = product;
    }

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("Product Details");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setVgap(12);
        grid.setHgap(10);
        grid.setPadding(new Insets(30));
        grid.setStyle("-fx-background-color: black;");

        Font labelFont = Font.font("Tahoma", 20);
        Font valueFont = Font.font("Tahoma", 20);

        int row = 0;
        // Product Details Section
        Label productSection = new Label("Product Information");
        productSection.setFont(Font.font("Tahoma", 22));
        productSection.setTextFill(Color.WHITE);
        grid.add(productSection, 0, row++, 2, 1);

        addDetail(grid, "ID:", String.valueOf(product.getId()), row++, labelFont, valueFont);
        addDetail(grid, "Name:", product.getName(), row++, labelFont, valueFont);
        addDetail(grid, "Quantity:", String.valueOf(product.getQuantity()), row++, labelFont, valueFont);
        addDetail(grid, "Brand:", product.getBrand(), row++, labelFont, valueFont);
        addDetail(grid, "Category:", product.getCategory(), row++, labelFont, valueFont);
        addDetail(grid, "Location:", product.getLocation(), row++, labelFont, valueFont);
        addDetail(grid, "Size (sqm):", String.valueOf(product.getSizeSqm()), row++, labelFont, valueFont);
        addDetail(grid, "Value ($):", String.valueOf(product.getValue()), row++, labelFont, valueFont);
        addDetail(grid, "Reorder Level:", String.valueOf(product.getReorderLevel()), row++, labelFont, valueFont);

        // Add spacing between sections
        row++;

        // Supplier Details Section
        Supplier supplier = product.getSupplier();
        if (supplier != null) {
            Label supplierSection = new Label("Supplier Information");
            supplierSection.setFont(Font.font("Tahoma", 22));
            supplierSection.setTextFill(Color.WHITE);
            grid.add(supplierSection, 0, row++, 2, 1);

            addDetail(grid, "Supplier ID:", String.valueOf(supplier.getId()), row++, labelFont, valueFont);
            addDetail(grid, "Supplier Name:", supplier.getName(), row++, labelFont, valueFont);
            addDetail(grid, "Contact Info:", supplier.getContactInfo() != null ? supplier.getContactInfo() : "N/A", row++, labelFont, valueFont);
            addDetail(grid, "Email:", supplier.getEmail() != null ? supplier.getEmail() : "N/A", row++, labelFont, valueFont);
            addDetail(grid, "Phone:", supplier.getPhone() != null ? supplier.getPhone() : "N/A", row++, labelFont, valueFont);
        }

        Scene scene = new Scene(grid, 600, 800);
        stage.setScene(scene);
        stage.show();
    }

    private void addDetail(GridPane grid, String labelText, String valueText, int row, Font labelFont, Font valueFont) {
        Label label = new Label(labelText);
        label.setTextFill(Color.WHITE);
        label.setFont(labelFont);

        Label value = new Label(valueText);
        value.setTextFill(Color.LIGHTGRAY);
        value.setFont(valueFont);

        grid.add(label, 0, row);
        grid.add(value, 1, row);
    }
}