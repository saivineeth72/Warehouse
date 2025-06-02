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
import org.springframework.web.client.RestTemplate;

import java.text.DecimalFormat;
import java.util.Random;

public class AdminDashboard extends Application {

    private static final String REMAINING_CAPACITY_URL = "http://localhost:8080/warehouse/remaining-capacity";
    private static final String TOTAL_QUANTITY_URL = "http://localhost:8080/warehouse/total-quantity";

    private static final int ROLL_FRAMES = 10;
    private static final int FRAME_DELAY_MS = 100;

    private Label remainingLabel;
    private Label quantityLabel;
    private String role;

    public AdminDashboard() {
        this.role = "admin"; // Default role
    }

    public AdminDashboard(String role) {
        this.role = role;
    }

    @Override
    public void start(Stage stage) {        

        // === Right side: Title + Animated Numbers ===
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

        // Create separate boxes for space and quantity
        VBox spaceBox = new VBox(20, title1, remainingLabel);
        spaceBox.setAlignment(Pos.CENTER_RIGHT);
        spaceBox.setPadding(new Insets(50));
        spaceBox.setStyle("-fx-background-color: black;");

        VBox quantityBox = new VBox(20, title2, quantityLabel);
        quantityBox.setAlignment(Pos.CENTER_RIGHT);
        quantityBox.setPadding(new Insets(50));
        quantityBox.setStyle("-fx-background-color: black;");

        // Container for both stat boxes
        VBox rightContent = new VBox(20);
        rightContent.getChildren().addAll(spaceBox, quantityBox);
        rightContent.setAlignment(Pos.CENTER);
        rightContent.setStyle("-fx-background-color: black;");

        // === Left menu: Labels as boxes ===
        VBox leftMenu = new VBox(30);
        leftMenu.setAlignment(Pos.CENTER_LEFT);
        leftMenu.setPadding(new Insets(40));
        leftMenu.setStyle("-fx-background-color: black;");

        if (!"employee".equals(role)) {
            Label suppliersMenu = createMenuLabel("Suppliers");
            suppliersMenu.setOnMouseClicked(e -> new SupplierWindow().show());
            leftMenu.getChildren().add(suppliersMenu);
        }

        Label inventoryMenu = createMenuLabel("Inventory");
        inventoryMenu.setOnMouseClicked(e -> new InventoryWindow().show());
        leftMenu.getChildren().add(inventoryMenu);

        Label addProductMenu = createMenuLabel("Buy a Product");
        addProductMenu.setOnMouseClicked(e -> new AddProductForm().show());
        leftMenu.getChildren().add(addProductMenu);

        Label salesMenu = createMenuLabel("Sell a product");
        salesMenu.setOnMouseClicked(e -> new SalesWindow().show());
        leftMenu.getChildren().add(salesMenu);

        Label warningMenu = createMenuLabel("Warnings");
        warningMenu.setOnMouseClicked(e -> new ForecastWarningWindow().show());
        leftMenu.getChildren().add(warningMenu);

        Label AnalyticsMenu = createMenuLabel("Analytics");
        AnalyticsMenu.setOnMouseClicked(e -> new Analytics().show());
        leftMenu.getChildren().add(AnalyticsMenu);

        Label TransactionMenu = createMenuLabel("Transactions");
        TransactionMenu.setOnMouseClicked(e -> new TransactionWindow().show());
        leftMenu.getChildren().add(TransactionMenu);

        // === Root Layout ===
        BorderPane root = new BorderPane();
        root.setLeft(leftMenu);
        root.setCenter(rightContent);
        root.setStyle("-fx-background-color: black;");

        Scene scene = new Scene(root, 1000, 600);
        stage.setScene(scene);
        stage.setTitle("Warehouse Dashboard");
        stage.show();

        fetchAndAnimateValues();
    }

    private Label createMenuLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        label.setTextFill(Color.WHITE);
        label.setAlignment(Pos.CENTER);
        label.setMinSize(280, 70);
        label.setStyle("-fx-background-color: transparent; -fx-border-color: white; -fx-border-radius: 5;");

        label.addEventHandler(MouseEvent.MOUSE_ENTERED, e ->
                label.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-border-radius: 5;"));
        label.addEventHandler(MouseEvent.MOUSE_EXITED, e ->
                label.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: white; -fx-border-radius: 5;"));
        return label;
    }

    private void fetchAndAnimateValues() {
        new Thread(() -> {
            try {
                RestTemplate restTemplate = new RestTemplate();
                Long capacity = restTemplate.getForObject(REMAINING_CAPACITY_URL, Long.class);
                Long quantity = restTemplate.getForObject(TOTAL_QUANTITY_URL, Long.class);

                System.out.println("Capacity: " + capacity);
                System.out.println("Quantity: " + quantity);

                if (capacity != null && quantity != null) {
                    Platform.runLater(() -> {
                        animateLabel(remainingLabel, capacity, "sq.mts");
                        animateLabel(quantityLabel, quantity, "units");
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    remainingLabel.setText("Error fetching space");
                    quantityLabel.setText("Error fetching quantity");
                });
            }
        }).start();
    }

    private void animateLabel(Label label, long finalValue, String suffix) {
        Random rand = new Random();
        Timeline timeline = new Timeline();
    
        for (int i = 0; i < ROLL_FRAMES; i++) {
            int frame = i;
            KeyFrame keyFrame = new KeyFrame(Duration.millis(FRAME_DELAY_MS * frame), event -> {
                long fake = (long) (rand.nextDouble() * finalValue * 1.5); // Generates a safe fake long value
                label.setText(formatNumber(fake) + " " + suffix);
            });
            timeline.getKeyFrames().add(keyFrame);
        }
    
        KeyFrame finalFrame = new KeyFrame(Duration.millis(ROLL_FRAMES * FRAME_DELAY_MS), event ->
                label.setText(formatNumber(finalValue) + " " + suffix));
        timeline.getKeyFrames().add(finalFrame);
    
        timeline.play();
    }

    private String formatNumber(long value) {
        DecimalFormat df = new DecimalFormat("###,###,###,###");
        return df.format(value);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
