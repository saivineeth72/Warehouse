package com.wms.warehouse.gui;

import com.wms.warehouse.model.Product;
import com.wms.warehouse.model.Supplier;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.layout.HBox;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

public class UpdateProductForm {

    private static final String UPDATE_URL = "http://localhost:8080/products/";
    private static final String SUPPLIERS_URL = "http://localhost:8080/suppliers";

    private final Product product;

    public UpdateProductForm(Product product) {
        this.product = product;
    }

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("Update Product");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(25);
        grid.setPadding(new Insets(40));
        grid.setStyle("-fx-background-color: black;");

        Font font = Font.font("Arial", 16);

        TextField nameField = createTextField("Product Name", grid, 0, font, product.getName());
        TextField quantityField = createTextField("Quantity", grid, 1, font, String.valueOf(product.getQuantity()));
        TextField brandField = createTextField("Brand", grid, 2, font, product.getBrand());
        TextField categoryField = createTextField("Category", grid, 3, font, product.getCategory());
        TextField locationField = createTextField("Location", grid, 4, font, product.getLocation());
        TextField sizeField = createTextField("Size (sqm)", grid, 5, font, String.valueOf(product.getSizeSqm()));
        TextField valueField = createTextField("Value ($)", grid, 6, font, product.getValue().toString());
        TextField reorderField = createTextField("Reorder Level", grid, 7, font, String.valueOf(product.getReorderLevel()));

        ComboBox<Supplier> supplierBox = new ComboBox<>();
        supplierBox.setPrefWidth(200);
        supplierBox.setPromptText("Select Supplier");

        grid.add(newLabel("Supplier:", font), 0, 8);
        grid.add(supplierBox, 1, 8);

        HBox buttons = new HBox(20);
        buttons.setAlignment(Pos.CENTER_RIGHT);

// === Update Button ===
        Button submit = new Button("Update");
        submit.setFont(font);
        submit.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-border-color: white; -fx-border-radius: 5;");
        submit.setOnMouseEntered(e -> submit.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-radius: 5;"));
        submit.setOnMouseExited(e -> submit.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-border-color: white; -fx-border-radius: 5;"));

// === Delete Button ===
        Button delete = new Button("Delete");
        delete.setFont(font);
        delete.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-border-color: white; -fx-border-radius: 5;");
        delete.setOnMouseEntered(e -> delete.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-radius: 5;"));
        delete.setOnMouseExited(e -> delete.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-border-color: white; -fx-border-radius: 5;"));

        delete.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this product?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    new Thread(() -> {
                        try {
                            RestTemplate restTemplate = new RestTemplate();
                            restTemplate.delete("http://localhost:8080/products/" + product.getId());
                            Platform.runLater(() -> {
                                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Product deleted.");
                                alert.showAndWait();
                                stage.close();
                            });
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            Platform.runLater(() -> {
                                Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to delete product.");
                                alert.showAndWait();
                            });
                        }
                    }).start();
                }
            });
        });

        submit.setOnAction(e -> {
            try {
                if (nameField.getText().isEmpty() || quantityField.getText().isEmpty() || supplierBox.getValue() == null) {
                    showAlert(Alert.AlertType.WARNING, "Please fill in all required fields.");
                    return;
                }

                Map<String, Object> updatedProduct = new HashMap<>();
                updatedProduct.put("id", product.getId());
                updatedProduct.put("name", nameField.getText());
                updatedProduct.put("quantity", Integer.parseInt(quantityField.getText()));
                updatedProduct.put("brand", brandField.getText());
                updatedProduct.put("category", categoryField.getText());
                updatedProduct.put("location", locationField.getText());
                updatedProduct.put("sizeSqm", Double.parseDouble(sizeField.getText()));
                updatedProduct.put("value", new java.math.BigDecimal(valueField.getText()));
                updatedProduct.put("reorderLevel", Integer.parseInt(reorderField.getText()));
                
                Map<String, Object> supplier = new HashMap<>();
                supplier.put("id", supplierBox.getValue().getId());
                updatedProduct.put("supplier", supplier);

                new Thread(() -> {
                    try {
                        RestTemplate restTemplate = new RestTemplate();
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);
                        HttpEntity<Map<String, Object>> request = new HttpEntity<>(updatedProduct, headers);
                        
                        System.out.println("Sending update request to: " + UPDATE_URL + product.getId());
                        System.out.println("Request body: " + updatedProduct);
                        
                        restTemplate.put(UPDATE_URL + product.getId(), request);
                        
                        Platform.runLater(() -> {
                            showAlert(Alert.AlertType.INFORMATION, "Product updated successfully!");
                            stage.close();
                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Platform.runLater(() -> {
                            String errorMessage = "Failed to update product: " + ex.getMessage();
                            System.err.println(errorMessage);
                            showAlert(Alert.AlertType.ERROR, errorMessage);
                        });
                    }
                }).start();

            } catch (NumberFormatException nfe) {
                showAlert(Alert.AlertType.ERROR, "Please enter valid numbers for quantity, size, value, and reorder level.");
            }
        });

        buttons.getChildren().addAll(submit, delete);
        grid.add(buttons, 1, 10);


        Scene scene = new Scene(grid, 500, 600);
        stage.setScene(scene);
        stage.show();

        loadSuppliers(supplierBox);
    }

    private void loadSuppliers(ComboBox<Supplier> box) {
        new Thread(() -> {
            try {
                RestTemplate restTemplate = new RestTemplate();
                Supplier[] suppliers = restTemplate.getForObject(SUPPLIERS_URL, Supplier[].class);
                if (suppliers != null) {
                    Platform.runLater(() -> {
                        box.getItems().setAll(Arrays.asList(suppliers));
                        box.setConverter(new javafx.util.StringConverter<>() {
                            @Override public String toString(Supplier s) {
                                return s.getId() + " - " + s.getName();
                            }
                            @Override public Supplier fromString(String string) {
                                return box.getItems().stream()
                                        .filter(s -> (s.getId() + " - " + s.getName()).equals(string))
                                        .findFirst().orElse(null);
                            }
                        });
                        if (product.getSupplier() != null) {
                            for (Supplier s : suppliers) {
                                if (s.getId().equals(product.getSupplier().getId())) {
                                    box.getSelectionModel().select(s);
                                    break;
                                }
                            }
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private Label newLabel(String text, Font font) {
        Label label = new Label(text);
        label.setTextFill(Color.WHITE);
        label.setFont(font);
        return label;
    }

    private TextField createTextField(String label, GridPane grid, int row, Font font, String value) {
        Label l = newLabel(label + ":", font);
        TextField tf = new TextField(value);
        tf.setFont(font);
        grid.add(l, 0, row);
        grid.add(tf, 1, row);
        return tf;
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type, message);
        alert.showAndWait();
    }
}
