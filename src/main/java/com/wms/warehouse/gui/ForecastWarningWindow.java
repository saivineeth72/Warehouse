package com.wms.warehouse.gui;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.web.client.RestTemplate;

import com.wms.warehouse.model.ProductDemand;

import java.util.Arrays;
import java.util.List;

public class ForecastWarningWindow extends Stage {

    public ForecastWarningWindow() {
        System.out.println("🟢 ForecastWarningWindow initialized");
    
        TableView<ProductDemand> table = new TableView<>();
    
        TableColumn<ProductDemand, String> nameCol = new TableColumn<>("Product");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
    
        TableColumn<ProductDemand, String> forecastCol = new TableColumn<>("Forecasted Demand");
        forecastCol.setCellValueFactory(new PropertyValueFactory<>("forecastedDemand"));
    
        TableColumn<ProductDemand, String> currentCol = new TableColumn<>("Current Stock");
        currentCol.setCellValueFactory(new PropertyValueFactory<>("currentQuantity"));
    
        table.getColumns().addAll(nameCol, forecastCol, currentCol);
    
        VBox root = new VBox(table);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: black;");
    
        setTitle("⚠️ Forecast Warnings");
        setScene(new Scene(root, 600, 400));
    
        System.out.println("🔄 Starting HTTP request to /forecast/warnings");
    
        new Thread(() -> {
            try {
                RestTemplate restTemplate = new RestTemplate();
                String response = restTemplate.getForObject("http://localhost:8080/forecast/warnings", String.class);
                System.out.println("🔍 Raw Response: " + response);
    
                ObjectMapper mapper = new ObjectMapper();
                List<ProductDemand> warnings = Arrays.asList(mapper.readValue(response, ProductDemand[].class));
                System.out.println("✅ Parsed " + warnings.size() + " warnings");
    
                Platform.runLater(() -> {
                    table.getItems().setAll(warnings);
                    System.out.println("📋 Table updated on UI thread");
                });
            } catch (Exception e) {
                System.err.println("❌ Exception in HTTP or parsing block:");
                e.printStackTrace();
            }
        }).start();
    }
}