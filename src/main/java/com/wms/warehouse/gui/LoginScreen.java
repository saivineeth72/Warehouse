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

import com.wms.warehouse.model.User;

public class LoginScreen extends Application {

    private static final String LOGIN_URL = "http://localhost:8080/api/login";
    private static final String REMAINING_CAPACITY_URL = "http://localhost:8080/warehouse/remaining-capacity";
    private static final String TOTAL_QUANTITY_URL = "http://localhost:8080/warehouse/total-quantity";
    
    private static final int ROLL_FRAMES = 10;
    private static final int FRAME_DELAY_MS = 100;

    private Label remainingLabel;
    private Label quantityLabel;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Warehouse Login");

        // === Right Stats ===
        Label title1 = new Label("Available space");
        title1.setFont(Font.font("courier", FontWeight.BOLD, 40));
        title1.setTextFill(Color.WHITE);

        remainingLabel = new Label("0000000000 sq.mts");
        remainingLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 50));
        remainingLabel.setTextFill(Color.WHITE);

        Label title2 = new Label("Quantity in storage");
        title2.setFont(Font.font("courier", FontWeight.BOLD, 40));
        title2.setTextFill(Color.WHITE);

        quantityLabel = new Label("0000000000 units");
        quantityLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 50));
        quantityLabel.setTextFill(Color.WHITE);

        VBox rightContent1 = new VBox(20, title1, remainingLabel);
        rightContent1.setAlignment(Pos.CENTER_RIGHT);
        rightContent1.setPadding(new Insets(50));
        rightContent1.setStyle("-fx-background-color: black;");

        VBox rightContent2 = new VBox(20, title2, quantityLabel);
        rightContent2.setAlignment(Pos.CENTER_RIGHT);
        rightContent2.setPadding(new Insets(50));
        rightContent2.setStyle("-fx-background-color: black;");

        // Create a container for both right content boxes
        VBox rightContainer = new VBox(20);
        rightContainer.getChildren().addAll(rightContent1, rightContent2);
        rightContainer.setAlignment(Pos.CENTER);
        rightContainer.setStyle("-fx-background-color: black;");

       
        Label WarehouseLabel = new Label("Warehouse");
        WarehouseLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 60));
        WarehouseLabel.setTextFill(Color.WHITE);

        Label ManagementLabel = new Label("Management");
        ManagementLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 60));
        ManagementLabel.setTextFill(Color.WHITE);

        Label SystemLabel = new Label("System");
        SystemLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 60));
        SystemLabel.setTextFill(Color.WHITE);

        // Create a VBox for the title with smaller spacing
        VBox titleBox = new VBox(5); // Reduce spacing between title words
        titleBox.getChildren().addAll(WarehouseLabel, ManagementLabel, SystemLabel);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        Label subtitle = new Label("Please login to continue");
        subtitle.setTextFill(Color.LIGHTGRAY);
        subtitle.setFont(Font.font("Arial", 16));
        subtitle.setPadding(new Insets(0, 0, 20, 0));

        // Username field with icon/styling
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setFont(Font.font("Arial", 16));
        usernameField.setStyle("""
            -fx-background-color: #333333;
            -fx-text-fill: white;
            -fx-prompt-text-fill: #888888;
            -fx-background-radius: 5;
            -fx-border-radius: 5;
            -fx-border-color: #444444;
            -fx-border-width: 1;
            -fx-padding: 10;
        """);

        // Password field with icon/styling
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setFont(Font.font("Arial", 16));
        passwordField.setStyle("""
            -fx-background-color: #333333;
            -fx-text-fill: white;
            -fx-prompt-text-fill: #888888;
            -fx-background-radius: 5;
            -fx-border-radius: 5;
            -fx-border-color: #444444;
            -fx-border-width: 1;
            -fx-padding: 10;
        """);

        // Login button with enhanced styling
        Button loginButton = new Button("Login");
        loginButton.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        loginButton.setPrefWidth(150);
        loginButton.setStyle("""
            -fx-background-color: #2196F3;
            -fx-text-fill: white;
            -fx-background-radius: 3;
            -fx-padding: 8;
            -fx-cursor: hand;
        """);
        loginButton.setOnMouseEntered(e -> loginButton.setStyle("""
            -fx-background-color: #1976D2;
            -fx-text-fill: white;
            -fx-background-radius: 3;
            -fx-padding: 8;
            -fx-cursor: hand;
        """));
        loginButton.setOnMouseExited(e -> loginButton.setStyle("""
            -fx-background-color: #2196F3;
            -fx-text-fill: white;
            -fx-background-radius: 3;
            -fx-padding: 8;
            -fx-cursor: hand;
        """));

        // Register link
        Label registerLink = new Label("Register");
        registerLink.setFont(Font.font("Arial", 14));
        registerLink.setTextFill(Color.web("#2196F3"));
        registerLink.setStyle("""
            -fx-underline: true;
            -fx-cursor: hand;
        """);
        registerLink.setOnMouseEntered(e -> {
            registerLink.setTextFill(Color.web("#1976D2"));
        });
        registerLink.setOnMouseExited(e -> {
            registerLink.setTextFill(Color.web("#2196F3"));
        });

        // Add the register action
        registerLink.setOnMouseClicked(e -> {
            new RegisterSupplierWindow().start(new Stage());
        });

        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.web("#FF5252"));
        errorLabel.setFont(Font.font("Arial", 14));

        // Spacing container for form elements
        VBox formBox = new VBox(20);
        formBox.setAlignment(Pos.CENTER_LEFT);
        formBox.getChildren().addAll(usernameField, passwordField);
        formBox.setPadding(new Insets(0, 0, 10, 0));

        // Container for login button (centered)
        HBox loginButtonBox = new HBox(loginButton);
        loginButtonBox.setAlignment(Pos.CENTER);

        // Container for register link (centered)
        HBox registerBox = new HBox(registerLink);
        registerBox.setAlignment(Pos.CENTER);

        // Create a VBox specifically for the form elements with fixed width
        VBox formContainer = new VBox(25);
        formContainer.setPrefWidth(300); // Set width for form elements only
        formContainer.getChildren().addAll(subtitle, formBox, loginButtonBox, registerBox, errorLabel);
        formContainer.setAlignment(Pos.CENTER_LEFT);

        // Main container
        VBox loginBox = new VBox(25);
        loginBox.setPadding(new Insets(50, 50, 50, 50));
        loginBox.setAlignment(Pos.CENTER_LEFT);
        loginBox.setStyle("-fx-background-color: black;");
        loginBox.setPrefWidth(600); // Increase overall width for the title
        loginBox.getChildren().addAll(titleBox, formContainer);

        // Remove wrap text since we don't need it anymore
        ManagementLabel.setWrapText(false);
        WarehouseLabel.setWrapText(false);
        SystemLabel.setWrapText(false);

        BorderPane root = new BorderPane();
        root.setLeft(loginBox);
        root.setCenter(rightContainer);
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
                            try {
                                if ("admin".equals(user.getRole())) {
                                    new AdminDashboard().start(new Stage());
                                } else {
                                    new SupplierDashboard(user.getUsername()).start(new Stage());
                                }
                                stage.close();
                            } catch (Exception ex) {
                                errorLabel.setText("Error launching dashboard");
                            }
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

        for (int i = 0; i < ROLL_FRAMES; i++) {
            int frame = i;
            KeyFrame keyFrame = new KeyFrame(Duration.millis(FRAME_DELAY_MS * frame), event -> {
                int fake = rand.nextInt((int) (finalValue * 1.5 + 1));
                label.setText(formatNumber(fake) + " " + suffix);
            });
            timeline.getKeyFrames().add(keyFrame);
        }

        KeyFrame finalFrame = new KeyFrame(Duration.millis(ROLL_FRAMES * FRAME_DELAY_MS), event ->
                label.setText(formatNumber(finalValue) + " " + suffix));
        timeline.getKeyFrames().add(finalFrame);

        timeline.play();
    }

    private String formatNumber(int value) {
        return new DecimalFormat("000,000,000,000").format(value);
    }

    public static class User {
        private String username;
        private String role;

        public String getUsername() { return username; }
        public String getRole() { return role; }
    }
}