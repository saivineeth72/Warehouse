package com.wms.warehouse.gui;

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
import org.springframework.data.repository.query.Param;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ForecastWarningWindow extends Stage {

    public ForecastWarningWindow() {
        try {
            initializeUI();
        } catch (Exception e) {
            System.err.println("Error initializing ForecastWarningWindow:");
            e.printStackTrace();
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Initialization Error");
                alert.setHeaderText("Failed to initialize Forecast Warning Window");
                alert.setContentText("Error: " + e.getMessage());
                alert.show();
            });
        }
    }

    private void initializeUI() {
        setTitle("Forecast Warnings");

        TableView<ProductDemand> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setStyle("-fx-background-color: #1a1a1a; -fx-text-background-color: white;");
        
        // Style the table header
        table.getStylesheets().add("data:text/css," + 
            ".table-view .column-header-background { -fx-background-color: #2d2d2d; }" +
            ".table-view .column-header, .table-view .filler { -fx-background-color: #2d2d2d; -fx-size: 35px; -fx-border-color: #3d3d3d; }" +
            ".table-view .column-header .label { -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; }" +
            ".table-view .table-row-cell { -fx-background-color: #1a1a1a; -fx-table-cell-border-color: #3d3d3d; }" +
            ".table-view .table-row-cell:selected { -fx-background-color: #0066cc; }" +
            ".table-view .table-row-cell:hover { -fx-background-color: #333333; }" +
            ".table-view .scroll-bar:vertical { -fx-background-color: #1a1a1a; }" +
            ".table-view .scroll-bar:vertical .thumb { -fx-background-color: #4d4d4d; }" +
            ".table-view .scroll-bar:vertical .track { -fx-background-color: #2d2d2d; }");

        TableColumn<ProductDemand, String> idCol = createStyledColumn("Product ID", "productId");
        idCol.setMinWidth(100);
        TableColumn<ProductDemand, String> nameCol = createStyledColumn("Product", "productName");
        TableColumn<ProductDemand, String> brandCol = createStyledColumn("Brand", "brand");
        brandCol.setMinWidth(120);
        TableColumn<ProductDemand, String> supplierCol = createStyledColumn("Supplier", "supplierName");
        supplierCol.setMinWidth(150);
        TableColumn<ProductDemand, String> forecastCol = createStyledColumn("Forecasted Demand", "forecastedDemand");
        forecastCol.setMinWidth(150);
        TableColumn<ProductDemand, String> currentCol = createStyledColumn("Current Stock", "currentQuantity");
        currentCol.setMinWidth(150);
        
        TableColumn<ProductDemand, Void> orderCol = new TableColumn<>("Auto-Order");
        orderCol.setCellFactory(tc -> new TableCell<>() {
            private final Button orderBtn = new Button("Auto-Order");
            {
                orderBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
                orderBtn.setOnAction(e -> {
                    ProductDemand demand = getTableView().getItems().get(getIndex());
                    int forecast = Integer.parseInt(demand.getForecastedDemand().replace(",", ""));
                    int current = Integer.parseInt(demand.getCurrentQuantity().replace(",", ""));
                    int shortfall = forecast - current;
                    if (shortfall <= 0) return;
                    // Call backend to create order
                    new Thread(() -> {
                        try {
                            RestTemplate rest = new RestTemplate();
                            String url = "http://localhost:8080/orders/auto?productId=" + demand.getProductId() + "&quantity=" + shortfall;
                            String resp = rest.postForObject(url, null, String.class);
                            Platform.runLater(() -> {
                                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Auto-order placed for '" + demand.getProductName() + "' (" + shortfall + ") units.", ButtonType.OK);
                                alert.showAndWait();
                            });
                        } catch (Exception ex) {
                            Platform.runLater(() -> {
                                Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to place auto-order: " + ex.getMessage(), ButtonType.OK);
                                alert.showAndWait();
                            });
                        }
                    }).start();
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    ProductDemand demand = getTableView().getItems().get(getIndex());
                    int forecast = Integer.parseInt(demand.getForecastedDemand().replace(",", ""));
                    int current = Integer.parseInt(demand.getCurrentQuantity().replace(",", ""));
                    setGraphic(forecast > current ? orderBtn : null);
                }
            }
        });
        orderCol.setMinWidth(120);
        
        table.getColumns().addAll(idCol, nameCol, brandCol, supplierCol, forecastCol, currentCol, orderCol);

        TextField searchField = new TextField();
        searchField.setPromptText("Search by product name...");
        searchField.setStyle("-fx-background-color: #222; -fx-text-fill: white; -fx-border-color: white;");
        searchField.setPrefWidth(400);

        HBox searchBox = new HBox(searchField);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setPadding(new Insets(10));

        ScrollPane scrollPane = new ScrollPane(table);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #1a1a1a;");
        scrollPane.setPrefHeight(350);

        VBox topPane = new VBox(10, searchBox, scrollPane);
        topPane.setPadding(new Insets(10));
        topPane.setStyle("-fx-background-color: #1a1a1a;");

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
        String stockColor = "white";      // White instead of blue

        BorderPane mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #1a1a1a;");
        mainLayout.setTop(topPane);
        mainLayout.setCenter(barChart);

        Scene scene = new Scene(mainLayout, 800, 800);
        
        // Add CSS styling for the chart
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

        // Load data in a separate thread
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
                        System.out.println("Setting table items, size: " + masterList.size());
                        
                        FilteredList<ProductDemand> filteredList = new FilteredList<>(masterList, p -> true);
                        
                        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                            String keyword = newVal.toLowerCase(Locale.ROOT);
                            filteredList.setPredicate(demand -> demand.getProductName().toLowerCase().contains(keyword));
                        });
                        
                        table.setItems(filteredList);
                        
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
                        showError("Data Processing Error", "Failed to process and display data", e.getMessage());
                    }
                });

            } catch (Exception e) {
                System.err.println("Error fetching or processing data:");
                e.printStackTrace();
                Platform.runLater(() -> showError("Data Loading Error", "Failed to load forecast warnings", e.getMessage()));
            }
        }).start();
    }

    private <T> TableColumn<ProductDemand, T> createStyledColumn(String title, String property) {
        TableColumn<ProductDemand, T> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        col.setStyle("-fx-alignment: CENTER;");
        col.setSortable(true);
        
        col.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: #1a1a1a;");
                } else {
                    setText(item.toString());
                    
                    // Get the current row's ProductDemand object
                    ProductDemand demand = getTableRow().getItem();
                    if (demand != null) {
                        int currentStock = Integer.parseInt(demand.getCurrentQuantity().replace(",", ""));
                        int forecast = Integer.parseInt(demand.getForecastedDemand().replace(",", ""));
                        
                        // If current stock is less than forecast, use red text
                        if (currentStock < forecast) {
                            setStyle("-fx-text-fill: #ff4444; -fx-alignment: center; -fx-font-size: 14px;");
                        } else {
                            setStyle("-fx-text-fill: white; -fx-alignment: center; -fx-font-size: 14px;");
                        }
                    } else {
                        setStyle("-fx-text-fill: white; -fx-alignment: center; -fx-font-size: 14px;");
                    }
                }
            }
        });
        return col;
    }

    private void showError(String title, String header, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.show();
        });
    }
}
