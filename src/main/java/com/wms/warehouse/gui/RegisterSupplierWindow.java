package com.wms.warehouse.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

public class RegisterSupplierWindow extends Application {

    private static final String REGISTER_URL = "http://localhost:8080/api/register-supplier";

    @Override
    public void start(Stage stage) {
        stage.setTitle("Register as Supplier");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setVgap(15);
        grid.setHgap(10);
        grid.setPadding(new Insets(40));
        grid.setStyle("-fx-background-color: black;");

        Label title = new Label("Supplier Registration");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        grid.add(title, 0, 0, 2, 1);

        TextField nameField = new TextField();
        nameField.setPromptText("Company Name");
        TextField contactField = new TextField();
        contactField.setPromptText("Contact Address");
        TextField emailField = new TextField();
        emailField.setPromptText("Email Address");
        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone Number");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Label statusLabel = new Label();
        statusLabel.setTextFill(Color.RED);

        Button registerButton = new Button("Register");
        registerButton.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        registerButton.setOnAction(e -> {
            if (nameField.getText().isEmpty() || contactField.getText().isEmpty() ||
                emailField.getText().isEmpty() || phoneField.getText().isEmpty() ||
                usernameField.getText().isEmpty() || passwordField.getText().isEmpty()) {
                statusLabel.setText("Please fill in all fields.");
                return;
            }

            Map<String, String> data = new HashMap<>();
            data.put("name", nameField.getText());
            data.put("contactInfo", contactField.getText());
            data.put("email", emailField.getText());
            data.put("phone", phoneField.getText());
            data.put("username", usernameField.getText());
            data.put("password", passwordField.getText());

            new Thread(() -> {
                try {
                    new RestTemplate().postForEntity(REGISTER_URL, data, String.class);
                    Platform.runLater(() -> {
                        statusLabel.setTextFill(Color.LIGHTGREEN);
                        statusLabel.setText("Registration successful! You can now log in.");
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() -> statusLabel.setText("Registration failed. Try again."));
                }
            }).start();
        });

        grid.add(new Label("Company Name:"), 0, 1); grid.add(nameField, 1, 1);
        grid.add(new Label("Contact Info:"), 0, 2); grid.add(contactField, 1, 2);
        grid.add(new Label("Email:"), 0, 3); grid.add(emailField, 1, 3);
        grid.add(new Label("Phone:"), 0, 4); grid.add(phoneField, 1, 4);
        grid.add(new Label("Username:"), 0, 5); grid.add(usernameField, 1, 5);
        grid.add(new Label("Password:"), 0, 6); grid.add(passwordField, 1, 6);
        grid.add(registerButton, 1, 7);
        grid.add(statusLabel, 1, 8);

        Scene scene = new Scene(grid, 600, 500);
        stage.setScene(scene);
        stage.show();
    }
} 
