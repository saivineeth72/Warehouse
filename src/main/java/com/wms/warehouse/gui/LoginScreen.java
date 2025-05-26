package com.wms.warehouse.gui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class LoginScreen extends Application {

    private static final String LOGIN_URL = "http://localhost:8080/api/login";
    private static final String REMAINING_CAPACITY_URL = "http://localhost:8080/warehouse/remaining-capacity";
    private static final String TOTAL_QUANTITY_URL = "http://localhost:8080/warehouse/total-quantity";

    private Label remainingLabel;
    private Label quantityLabel;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Warehouse Login");

        // === Right Stats ===
        Label title1 = new Label("Available space");
        title1.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title1.setTextFill(Color.WHITE);

        remainingLabel = new Label("0000000000 sq.mts");
        remainingLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 40));
        remainingLabel.setTextFill(Color.WHITE);

        Label title2 = new Label("Quantity in storage");
        title2.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title2.setTextFill(Color.WHITE);

        quantityLabel = new Label("0000000000 units");
        quantityLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 40));
        quantityLabel.setTextFill(Color.WHITE);

        VBox rightContent = new VBox(40, title1, remainingLabel, title2, quantityLabel);
        rightContent.setAlignment(Pos.CENTER_RIGHT);
        rightContent.setPadding(new Insets(40));
        rightContent.setStyle("-fx-background-color: black;");

        // === Left Login ===
        Label title = new Label("Login");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setFont(Font.font("Arial", 16));

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setFont(Font.font("Arial", 16));

        Button loginButton = new Button("Login");
        loginButton.setFont(Font.font("Arial", 16));
        styleButton(loginButton);

        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);

        VBox loginBox = new VBox(20, title, usernameField, passwordField, loginButton, errorLabel);
        loginBox.setPadding(new Insets(40));
        loginBox.setAlignment(Pos.CENTER_LEFT);
        loginBox.setStyle("-fx-background-color: black;");

        BorderPane root = new BorderPane();
        root.setLeft(loginBox);
        root.setCenter(rightContent);
        root.setStyle("-fx-background-color: black;");

        Scene scene = new Scene(root, 1000, 600);
        stage.setScene(scene);
        stage.show();

        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();

            if (username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Please enter both fields.");
                return;
            }

            Map<String, String> credentials = new HashMap<>();
            credentials.put("username", username);
            credentials.put("password", password);

            new Thread(() -> {
                try {
                    RestTemplate rest = new RestTemplate();
                    ResponseEntity<User> response = rest.postForEntity(LOGIN_URL, credentials, User.class);
                    User user = response.getBody();
                    if (user != null) {
                        Platform.runLater(() -> {
                            if ("admin".equals(user.getRole())) {
                                new AdminDashboard().start(new Stage());
                            } else {
                                new SupplierDashboard().start(new Stage());
                            }
                            stage.close();
                        });
                    }
                } catch (Exception ex) {
                    Platform.runLater(() -> errorLabel.setText("Invalid credentials or server error."));
                }
            }).start();
        });

        fetchAndAnimateStats();
    }

    private void fetchAndAnimateStats() {
        new Thread(() -> {
            try {
                RestTemplate rest = new RestTemplate();
                Double capacity = rest.getForObject(REMAINING_CAPACITY_URL, Double.class);
                Long quantity = rest.getForObject(TOTAL_QUANTITY_URL, Long.class);

                if (capacity != null && quantity != null) {
                    Platform.runLater(() -> {
                        animateLabel(remainingLabel, capacity.intValue(), "sq.mts");
                        animateLabel(quantityLabel, quantity.intValue(), "units");
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    remainingLabel.setText("Error fetching space");
                    quantityLabel.setText("Error fetching quantity");
                });
            }
        }).start();
    }

    private void animateLabel(Label label, int finalValue, String suffix) {
        Random rand = new Random();
        Timeline timeline = new Timeline();

        for (int i = 0; i < 10; i++) {
            int frame = i;
            timeline.getKeyFrames().add(new KeyFrame(Duration.millis(100 * frame), e -> {
                int fake = rand.nextInt(finalValue + 1);
                label.setText(formatNumber(fake) + " " + suffix);
            }));
        }

        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(1000), e -> label.setText(formatNumber(finalValue) + " " + suffix)));
        timeline.play();
    }

    private String formatNumber(int value) {
        return new DecimalFormat("000,000,000").format(value);
    }

    private void styleButton(Button btn) {
        btn.setStyle("-fx-background-color: white; -fx-text-fill: black;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: black; -fx-text-fill: white;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: white; -fx-text-fill: black;"));
    }

    public static class User {
        private String username;
        private String role;

        public String getUsername() { return username; }
        public String getRole() { return role; }
    }
}