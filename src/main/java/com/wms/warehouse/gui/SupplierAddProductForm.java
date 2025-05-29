
package com.wms.warehouse.gui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

public class SupplierAddProductForm {

    private static final String ADD_PRODUCT_URL = "http://localhost:8080/products";
    private static final String SUPPLIERS_URL = "http://localhost:8080/suppliers";

    public static void show() {
        Stage stage = new Stage();
        stage.setTitle("Add New Product");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25));
        grid.setStyle("-fx-background-color: black;");

        Font font = Font.font("Arial", 14);

        TextField nameField = createTextField("Product Name", grid, 0, font);
        TextField quantityField = createTextField("Quantity", grid, 1, font);
        TextField brandField = createTextField("Brand", grid, 2, font);
        TextField categoryField = createTextField("Category", grid, 3, font);
        TextField locationField = createTextField("Location", grid, 4, font);
        TextField sizeField = createTextField("Size (sqm)", grid, 5, font);
        TextField valueField = createTextField("Value ($)", grid, 6, font);
        TextField reorderField = createTextField("Reorder Level", grid, 7, font);

        Label supplierLabel = new Label("Supplier:");
        supplierLabel.setTextFill(Color.WHITE);
        supplierLabel.setFont(font);
        ComboBox<Supplier> supplierBox = new ComboBox<>();
        supplierBox.setPrefWidth(200);
        grid.add(supplierLabel, 0, 8);
        grid.add(supplierBox, 1, 8);

        new Thread(() -> {
            try {
                RestTemplate rest = new RestTemplate();
                Supplier[] suppliers = rest.getForObject(SUPPLIERS_URL, Supplier[].class);
                if (suppliers != null) {
                    Platform.runLater(() -> supplierBox.setItems(FXCollections.observableArrayList(suppliers)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        Button submit = new Button("Submit");
        submit.setStyle("-fx-background-color: white; -fx-text-fill: black;");
        submit.setOnAction(e -> {
            try {
                if (nameField.getText().isEmpty() || quantityField.getText().isEmpty() || supplierBox.getValue() == null) {
                    showAlert(Alert.AlertType.WARNING, "Please fill in all required fields.");
                    return;
                }

                Map<String, Object> product = new HashMap<>();
                product.put("name", nameField.getText());
                product.put("quantity", Integer.parseInt(quantityField.getText()));
                product.put("brand", brandField.getText());
                product.put("category", categoryField.getText());
                product.put("location", locationField.getText());
                product.put("sizeSqm", Double.parseDouble(sizeField.getText()));
                product.put("value", Double.parseDouble(valueField.getText()));
                product.put("reorderLevel", Integer.parseInt(reorderField.getText()));
                Map<String, Object> supplier = new HashMap<>();
                supplier.put("id", supplierBox.getValue().getId());
                product.put("supplier", supplier);

                new Thread(() -> {
                    try {
                        RestTemplate restTemplate = new RestTemplate();
                        System.out.println("➡️ Payload: " + product);
                        System.out.println("➡️ Rest Template: " + restTemplate);
                        ResponseEntity<String> response = restTemplate.postForEntity(ADD_PRODUCT_URL, product, String.class);
                        Platform.runLater(() -> {
                            showAlert(Alert.AlertType.INFORMATION, "Product added successfully!");
                            stage.close();
                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Failed to add product."));
                    }
                }).start();

            } catch (NumberFormatException nfe) {
                showAlert(Alert.AlertType.ERROR, "Invalid number format in input fields.");
            }
        });

        grid.add(submit, 1, 10);
        Scene scene = new Scene(grid, 500, 600);
        stage.setScene(scene);
        stage.show();
    }

    private static TextField createTextField(String label, GridPane grid, int row, Font font) {
        Label l = new Label(label + ":");
        l.setTextFill(Color.WHITE);
        l.setFont(font);
        TextField tf = new TextField();
        tf.setFont(font);
        grid.add(l, 0, row);
        grid.add(tf, 1, row);
        return tf;
    }

    private static void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type, message);
        alert.showAndWait();
    }

    public static class Supplier {
        private Long id;
        private String name;

        public Long getId() { return id; }
        public String getName() { return name; }

        @Override
        public String toString() {
            return name + " (ID: " + id + ")";
        }
    }
}
