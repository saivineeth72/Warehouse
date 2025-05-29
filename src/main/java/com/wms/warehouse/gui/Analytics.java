package com.wms.warehouse.gui;

import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wms.warehouse.model.ProductDemand;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class Analytics extends Stage {

    public Analytics() {
        setTitle("🚚 Top 10 Fastest Suppliers");

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Forecast vs Stock");
        xAxis.setLabel("");
        yAxis.setLabel("");
        
        // Style the chart and its components
        barChart.setStyle("-fx-background-color: black;");
        barChart.setTitleSide(Side.TOP);
        barChart.setAnimated(false);
        barChart.setLegendVisible(true);
        
        xAxis.setStyle("-fx-tick-label-fill: white; -fx-text-fill: white;");
        yAxis.setStyle("-fx-tick-label-fill: white; -fx-text-fill: white;");
        
        // Set series colors
        String forecastColor = "#4CAF50";  // Green
        String stockColor = "white";



        // --- Fastest Suppliers Table ---
        TableView<Map.Entry<String, Double>> fastestTable = new TableView<>();
        fastestTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        fastestTable.setStyle("-fx-background-color: #1a1a1a; -fx-text-background-color: white;");

        TableColumn<Map.Entry<String, Double>, String> supplierCol = new TableColumn<>("Fastest Suppliers");
        supplierCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getKey()));
        supplierCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Map.Entry<String, Double>, Double> daysCol = new TableColumn<>("Avg Delivery Days");
        daysCol.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getValue()).asObject());
        daysCol.setStyle("-fx-alignment: CENTER;");

        supplierCol.setMinWidth(200);
        daysCol.setMinWidth(150);
        fastestTable.getColumns().addAll(supplierCol);

        // --- Top Products Table ---
        TableView<Map.Entry<String, Integer>> productTable = new TableView<>();
        productTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        productTable.setStyle("-fx-background-color: #1a1a1a; -fx-text-background-color: white;");

        TableColumn<Map.Entry<String, Integer>, String> productCol = new TableColumn<>("Best Selling Products");
        productCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getKey()));
        productCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Map.Entry<String, Integer>, Integer> quantityCol = new TableColumn<>("Total Demand");
        quantityCol.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getValue()).asObject());
        quantityCol.setStyle("-fx-alignment: CENTER;");

        productCol.setMinWidth(200);
        quantityCol.setMinWidth(150);
        productTable.getColumns().addAll(productCol);

        productTable.getStylesheets().add("data:text/css," +
            ".table-view .column-header-background { -fx-background-color: #2d2d2d; }" +
            ".table-view .column-header, .table-view .filler { -fx-background-color: #2d2d2d; -fx-size: 65px; -fx-border-color: #3d3d3d; }" +
            ".table-view .column-header .label { -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 24px; }" +
            ".table-view .table-row-cell { -fx-background-color: #1a1a1a; -fx-table-cell-border-color: #3d3d3d; -fx-font-size: 24px;}" +
            ".table-view .table-row-cell:selected { -fx-background-color: #0066cc; }" +
            ".table-view .table-row-cell:hover { -fx-background-color: #333333; }" +
            ".table-view .scroll-bar:vertical { -fx-background-color: #1a1a1a; }" +
            ".table-view .scroll-bar:vertical .thumb { -fx-background-color: #4d4d4d; }" +
            ".table-view .scroll-bar:vertical .track { -fx-background-color: #2d2d2d; }");

        fastestTable.getStylesheets().add("data:text/css," +
            ".table-view .column-header-background { -fx-background-color: #2d2d2d; }" +
            ".table-view .column-header, .table-view .filler { -fx-background-color: #2d2d2d; -fx-size: 65px; -fx-border-color: #3d3d3d; }" +
            ".table-view .column-header .label { -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 24px; }" +
            ".table-view .table-row-cell { -fx-background-color: #1a1a1a; -fx-table-cell-border-color: #3d3d3d; -fx-font-size: 24px;}" +
            ".table-view .table-row-cell:selected { -fx-background-color: #0066cc; }" +
            ".table-view .table-row-cell:hover { -fx-background-color: #333333; }" +
            ".table-view .scroll-bar:vertical { -fx-background-color: #1a1a1a; }" +
            ".table-view .scroll-bar:vertical .thumb { -fx-background-color: #4d4d4d; }" +
            ".table-view .scroll-bar:vertical .track { -fx-background-color: #2d2d2d; }");

        Label fastestTitle = new Label("Top Fastest Suppliers");
        fastestTitle.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-font-weight: bold;");

        Label topProductsTitle = new Label("Top Most Sold Products");
        topProductsTitle.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-font-weight: bold;");

        VBox leftPane = new VBox(10, fastestTable);
        VBox rightPane = new VBox(10, productTable);
        leftPane.setPadding(new Insets(10));
        rightPane.setPadding(new Insets(10));
        leftPane.setStyle("-fx-background-color: #1a1a1a;");
        rightPane.setStyle("-fx-background-color: #1a1a1a;");
            
        VBox.setVgrow(fastestTable, Priority.ALWAYS);
        VBox.setVgrow(productTable, Priority.ALWAYS);     
                  
        
            // --- Top Pane: Split View of Fastest Suppliers and Top Products ---
            HBox topSection = new HBox(20, leftPane, rightPane);
            topSection.setPadding(new Insets(10));
            topSection.setStyle("-fx-background-color: #1a1a1a;");
            topSection.setPrefHeight(400); // or bind to half of screen
            
            HBox.setHgrow(leftPane, Priority.ALWAYS);
            HBox.setHgrow(rightPane, Priority.ALWAYS);

        // Use this in the layout

        BorderPane mainLayout = new BorderPane();
        mainLayout.setTop(topSection);     // tables
        mainLayout.setCenter(barChart);
        mainLayout.setStyle("-fx-background-color: #1a1a1a;");

        Scene scene = new Scene(mainLayout, 800, 800);

        scene.getStylesheets().add("data:text/css," +
            ".chart { -fx-background-color: black; }" +
            ".chart-plot-background { -fx-background-color: black; }" +
            ".chart-title { -fx-text-fill: white; }" +
            ".chart-legend { -fx-background-color: transparent; }" +
            ".chart-legend-item { -fx-text-fill: white; }" +
            ".chart-horizontal-grid-lines { -fx-stroke: #404040; }" +
            ".chart-vertical-grid-lines { -fx-stroke: #404040; }" +
            ".axis { -fx-tick-label-fill: white; }" +
            ".axis-label { -fx-text-fill: white; }" +
            // Add specific styles for the series
            ".default-color0.chart-bar { -fx-bar-fill: " + forecastColor + "; }" +
            ".default-color1.chart-bar { -fx-bar-fill: " + stockColor + "; }");

        setScene(scene);
        fetchTopAnalytics(fastestTable, productTable);

        new Thread(() -> {
            try {
                System.out.println("Fetching forecast warnings...");
                RestTemplate restTemplate = new RestTemplate();
                String response = restTemplate.getForObject("http://localhost:8080/forecast/warnings", String.class);
                System.out.println("Raw API Response: " + response);
                
                ObjectMapper mapper = new ObjectMapper();
                List<ProductDemand> data = Arrays.asList(mapper.readValue(response, ProductDemand[].class));
                System.out.println("Parsed data size: " + data.size());
                
                if (data.isEmpty()) {
                    System.out.println("Warning: No data received from API");
                } else {
                    System.out.println("First item: " + data.get(0));
                }

                Platform.runLater(() -> {
                    try {
                        ObservableList<ProductDemand> masterList = FXCollections.observableArrayList(data);      
                        // Clear existing chart data
                        barChart.getData().clear();

                        XYChart.Series<String, Number> forecastSeries = new XYChart.Series<>();
                        forecastSeries.setName("Forecast");
                        XYChart.Series<String, Number> stockSeries = new XYChart.Series<>();
                        stockSeries.setName("In Stock");

                        for (ProductDemand pd : masterList) {
                            System.out.println("Processing item: " + pd.getProductName());
                            forecastSeries.getData().add(new XYChart.Data<>(
                                pd.getProductName(), 
                                Integer.parseInt(pd.getForecastedDemand().replace(",", ""))
                            ));
                            stockSeries.getData().add(new XYChart.Data<>(
                                pd.getProductName(), 
                                Integer.parseInt(pd.getCurrentQuantity().replace(",", ""))
                            ));
                        }

                        barChart.getData().addAll(forecastSeries, stockSeries);
                        
                        // Style the series after adding them
                        Platform.runLater(() -> {
                            forecastSeries.getNode().setStyle("-fx-bar-fill: " + forecastColor + ";");
                            stockSeries.getNode().setStyle("-fx-bar-fill: " + stockColor + ";");
                            
                            // Apply styles to each bar in the series
                            forecastSeries.getData().forEach(d -> 
                                d.getNode().setStyle("-fx-bar-fill: " + forecastColor + ";"));
                            stockSeries.getData().forEach(d -> 
                                d.getNode().setStyle("-fx-bar-fill: " + stockColor + ";"));
                        });
                    } catch (Exception e) {
                        System.err.println("Error updating UI with data:");
                        e.printStackTrace();
                    }
                });

            } catch (Exception e) {
                System.err.println("Error fetching or processing data:");
                e.printStackTrace();
            }
        }).start();
    }

    private void fetchTopAnalytics(TableView<Map.Entry<String, Double>> fastestTable, TableView<Map.Entry<String, Integer>> productTable) {
        new Thread(() -> {
            try {
                RestTemplate rest = new RestTemplate();
    
                // Fastest Suppliers
                List<Map<String, Double>> fastestRaw = rest.exchange(
                    "http://localhost:8080/supplier-performance/top-fastest",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Map<String, Double>>>() {}
                ).getBody();
    
                List<Map.Entry<String, Double>> fastestData = new ArrayList<>();
                if (fastestRaw != null) {
                    for (Map<String, Double> entry : fastestRaw) {
                        fastestData.addAll(entry.entrySet());
                    }
                }
    
                // Top Products
                List<Map<String, Integer>> productRaw = rest.exchange(
                    "http://localhost:8080/supplier-performance/top-products",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Map<String, Integer>>>() {}
                ).getBody();
    
                List<Map.Entry<String, Integer>> productData = new ArrayList<>();
                if (productRaw != null) {
                    for (Map<String, Integer> entry : productRaw) {
                        productData.addAll(entry.entrySet());
                    }
                }
    
                Platform.runLater(() -> {
                    fastestTable.setItems(FXCollections.observableArrayList(fastestData));
                    productTable.setItems(FXCollections.observableArrayList(productData));
                });
    
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to fetch analytics data.");
                    alert.show();
                });
            }
        }).start();
    }
}


