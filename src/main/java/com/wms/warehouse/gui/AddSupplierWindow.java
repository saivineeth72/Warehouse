package com.wms.warehouse.gui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

public class AddSupplierWindow {

    private static final String SUPPLIERS_URL = "http://localhost:8080/suppliers";

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("Add Supplier");

        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(25));
        layout.setStyle("-fx-background-color: black;");

        Font font = Font.font("Arial", 14);

        TextField nameField = createTextField("Name", layout, font);
        TextField emailField = createTextField("Email", layout, font);
        TextField phoneField = createTextField("Phone", layout, font);

        Button addButton = new Button("Add");
        addButton.setStyle("-fx-background-color: white; -fx-text-fill: black;");
        addButton.setOnAction(e -> {
            if (nameField.getText().isEmpty() || emailField.getText().isEmpty() || phoneField.getText().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Please fill in all fields.");
                return;
            }

            Map<String, String> supplier = new HashMap<>();
            supplier.put("name", nameField.getText());
            supplier.put("email", emailField.getText());
            supplier.put("phone", phoneField.getText());

            new Thread(() -> {
                try {
                    RestTemplate restTemplate = new RestTemplate();
                    restTemplate.postForEntity(SUPPLIERS_URL, supplier, String.class);
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.INFORMATION, "Supplier added successfully!");
                        stage.close();
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Failed to add supplier."));
                }
            }).start();
        });

        layout.getChildren().add(addButton);
        Scene scene = new Scene(layout, 400, 300);
        stage.setScene(scene);
        stage.show();
    }

    private TextField createTextField(String label, VBox layout, Font font) {
        Label l = new Label(label + ":");
        l.setTextFill(Color.WHITE);
        l.setFont(font);
        TextField tf = new TextField();
        tf.setFont(font);
        layout.getChildren().addAll(l, tf);
        return tf;
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type, message);
        alert.showAndWait();
    }
} 