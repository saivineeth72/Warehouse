package com.wms.warehouse.gui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wms.warehouse.model.Product;
import com.wms.warehouse.model.SalesHistory;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SalesWindow extends Stage {

    private final TableView<Product> productTable = new TableView<>();
    private final TableView<Map<String, Object>> supplierTable = new TableView<>();
    private final TextField searchField = new TextField();
    private final TextField quantityField = new TextField();
    private final Label availableLabel = new Label("Available:  N/A");
    private final Label totalCostLabel = new Label("Total Cost: $");
    private final Button sellButton = new Button("Sell");

    public SalesWindow() {
        setTitle("💳 Product Sale with Breakdown");

        // === Left Panel ===
        VBox leftPanel = new VBox(20);
        leftPanel.setPadding(new Insets(25));
        leftPanel.setStyle("-fx-background-color: black;");
        leftPanel.setAlignment(Pos.CENTER);

        searchField.setPromptText("Search product...");
        searchField.setStyle("-fx-background-color: #222; -fx-text-fill: white; -fx-font-size: 16px; -fx-prompt-text-fill: #808080;");
        searchField.setPrefWidth(300);
        searchField.setPrefHeight(40);

        quantityField.setPromptText("Quantity");
        quantityField.setStyle("-fx-background-color: #222; -fx-text-fill: white; -fx-font-size: 16px; -fx-prompt-text-fill: #808080;");
        quantityField.setPrefWidth(150);
        quantityField.setPrefHeight(40);

        availableLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
        availableLabel.setPrefWidth(200);
        availableLabel.setAlignment(Pos.CENTER_LEFT);

        sellButton.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-font-size: 16px; -fx-padding: 10 20; -fx-border-color: white;");
        sellButton.setOnMouseEntered(e -> sellButton.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 10 20; -fx-border-color: white;"));
        sellButton.setOnMouseExited(e -> sellButton.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-font-size: 16px; -fx-padding: 10 20; -fx-border-color: white;"));
        sellButton.setPrefHeight(40);
        sellButton.setPrefWidth(100);
        sellButton.setDisable(true);

        HBox inputBar = new HBox(15, searchField, quantityField, availableLabel, sellButton);
        inputBar.setAlignment(Pos.CENTER_LEFT);
        inputBar.setPadding(new Insets(0, 0, 15, 0));

        TableColumn<Product, String> nameCol = new TableColumn<>("Product");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 20px; -fx-font-family: 'Tahoma'; -fx-font-weight: bold; -fx-alignment: CENTER-LEFT;");
        nameCol.setCellFactory(tc -> {
            TableCell<Product, String> cell = new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item);
                    setAlignment(Pos.CENTER_LEFT);
                    if (getTableRow() != null && getTableRow().isSelected()) {
                        setStyle("-fx-text-fill: black; -fx-font-size: 16px; -fx-font-family: 'Tahoma'; -fx-background-color: white; -fx-padding: 0 0 0 10;");
                    } else {
                        setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-family: 'Tahoma'; -fx-background-color: black; -fx-padding: 0 0 0 10;");
                    }
                }
            };
            
            // Update cell style when row selection changes
            cell.tableRowProperty().addListener((obs, oldRow, newRow) -> {
                if (newRow != null) {
                    newRow.selectedProperty().addListener((obs2, wasSelected, isNowSelected) -> {
                        if (cell.getItem() != null) {
                            if (isNowSelected) {
                                cell.setStyle("-fx-text-fill: black; -fx-font-size: 16px; -fx-font-family: 'Tahoma'; -fx-background-color: white; -fx-padding: 0 0 0 10;");
                            } else {
                                cell.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-family: 'Tahoma'; -fx-background-color: black; -fx-padding: 0 0 0 10;");
                            }
                        }
                    });
                }
            });
            
            return cell;
        });

        productTable.getColumns().add(nameCol);
        productTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        productTable.setStyle("-fx-background-color: black; -fx-control-inner-background: black; -fx-table-cell-border-color: #333; -fx-table-header-border-color: #333; -fx-text-fill: white;");
        productTable.setRowFactory(tv -> {
            TableRow<Product> row = new TableRow<>();
            row.setStyle("-fx-background-color: black; -fx-border-color: #333; -fx-border-width: 0 0 1 0;");
            
            // Style for hover state
            row.setOnMouseEntered(e -> {
                if (!row.isSelected()) {
                    row.setStyle("-fx-background-color: #322; -fx-border-color: #333; -fx-border-width: 0 0 1 0;");
                }
            });
            
            row.setOnMouseExited(e -> {
                if (!row.isSelected()) {
                    row.setStyle("-fx-background-color: black; -fx-border-color: #333; -fx-border-width: 0 0 1 0;");
                }
            });

            // Style for selection state
            row.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
                if (isNowSelected) {
                    row.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-border-color: yellow; -fx-border-width: 1;");
                } else {
                    row.setStyle("-fx-background-color: black; -fx-border-color: #333; -fx-border-width: 0 0 1 0;");
                }
            });
            return row;
        });

        // Ensure selection model is set to single selection
        productTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        
        // Make the table focusable and maintain selection
        productTable.setFocusTraversable(true);
        
        // Update selection handling in the listener
        productTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            sellButton.setDisable(selected == null);
            if (selected != null) {
                DecimalFormat qtyFormatter = new DecimalFormat("#,###");
                availableLabel.setText("Available: " + qtyFormatter.format(selected.getQuantity()));
                productTable.requestFocus();
            } else {
                availableLabel.setText("Available:  N/A");
            }
        });

        leftPanel.getChildren().addAll(inputBar, productTable);

        // === Right Panel ===
        VBox rightPanel = new VBox(20);
        rightPanel.setPadding(new Insets(25));
        rightPanel.setStyle("-fx-background-color: black;");
        rightPanel.setAlignment(Pos.CENTER);

        Label breakdownLabel = new Label("🔍 Purchase Breakdown");
        breakdownLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        totalCostLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");

        supplierTable.setStyle("-fx-background-color: black; -fx-control-inner-background: black; -fx-table-cell-border-color: #333; -fx-table-header-border-color: #333; -fx-text-fill: white;");
        supplierTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        supplierTable.setRowFactory(tv -> {
            TableRow<Map<String, Object>> row = new TableRow<>();
            row.setStyle("-fx-background-color: black; -fx-border-color: #333; -fx-border-width: 0 0 1 0;");
            row.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
                row.setStyle(isNowSelected ?
                        "-fx-background-color: #333; -fx-border-color: #333; -fx-border-width: 0 0 1 0;" :
                        "-fx-background-color: black; -fx-border-color: #333; -fx-border-width: 0 0 1 0;");
            });
            return row;
        });

        supplierTable.getColumns().addAll(
            createCol("Product", "product"),
            createCol("Supplier", "supplier"),
            createCol("Location", "location"),
            createCol("Quantity", "quantity"),
            createCol("Unit Value", "unitValue"),
            createCol("Total", "total")
        );

        rightPanel.getChildren().addAll(breakdownLabel, supplierTable, totalCostLabel);

        // === Root Layout ===
        HBox root = new HBox(leftPanel, rightPanel);
        HBox.setHgrow(leftPanel, Priority.ALWAYS);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        leftPanel.setPrefWidth(600);
        rightPanel.setPrefWidth(600);

        double tableHeight = 560;
        productTable.setPrefHeight(tableHeight);
        supplierTable.setPrefHeight(tableHeight);

        Scene scene = new Scene(root, 1200, 800);
        scene.setFill(javafx.scene.paint.Color.web("#1a1a1a"));
        scene.getStylesheets().add(getClass().getResource("/dark-theme.css").toExternalForm());
        setScene(scene);

        // === Event Handlers ===
        fetchProducts("");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> fetchProducts(newVal));

        sellButton.setOnAction(e -> {
            Product selected = productTable.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            int qty;
            try {
                qty = Integer.parseInt(quantityField.getText());
                if (qty <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                showAlert("Please enter a valid positive quantity.");
                return;
            }
            handleSell(selected.getName(), qty);
        });

        fetchSalesHistory();
    }

    private void fetchProducts(String query) {
        new Thread(() -> {
            try {
                RestTemplate rest = new RestTemplate();
                String url = "http://localhost:8080/sales/search?keyword=" +
                        URLEncoder.encode(query, StandardCharsets.UTF_8);
                Product[] products = rest.getForObject(url, Product[].class);
                Platform.runLater(() -> productTable.setItems(FXCollections.observableArrayList(products)));
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert("Failed to fetch products"));
            }
        }).start();
    }

    private void handleSell(String productName, int quantity) {
        new Thread(() -> {
            try {
                RestTemplate rest = new RestTemplate();
                String url = "http://localhost:8080/sales/purchase?productName=" +
                        URLEncoder.encode(productName, StandardCharsets.UTF_8) +
                        "&quantityRequested=" + quantity;

                ObjectMapper mapper = new ObjectMapper();
                String json = rest.postForObject(url, null, String.class);
                List<Map<String, Object>> result = mapper.readValue(json, new TypeReference<>() {});
                double total = result.stream().mapToDouble(row -> ((Number) row.get("total")).doubleValue()).sum();

                Platform.runLater(() -> {
                    supplierTable.setItems(FXCollections.observableArrayList(result));
                    DecimalFormat formatter = new DecimalFormat("#,###.00");
                    totalCostLabel.setText("Total Cost: $" + formatter.format(total));
                    fetchProducts(searchField.getText());
                    quantityField.clear();
                    fetchSalesHistory();
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert("Sale failed or insufficient stock."));
            }
        }).start();
    }

    private TableColumn<Map<String, Object>, String> createCol(String title, String key) {
        TableColumn<Map<String, Object>, String> col = new TableColumn<>(title);
        col.setCellValueFactory(data -> {
            Object value = data.getValue().get(key);
            if (key.equals("supplier")) {
                // Handle supplier name extraction
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    Map<String, Object> supplierMap = mapper.convertValue(value, Map.class);
                    return new ReadOnlyStringWrapper((String) supplierMap.get("name"));
                } catch (Exception e) {
                    return new ReadOnlyStringWrapper(String.valueOf(value));
                }
            } else if (value instanceof Number) {
                DecimalFormat formatter = new DecimalFormat("#,###.00");
                return new ReadOnlyStringWrapper(formatter.format(((Number) value).doubleValue()));
            } else {
                return new ReadOnlyStringWrapper(String.valueOf(value));
            }
        });
        col.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-family: 'Tahoma'; -fx-alignment: CENTER;");
        col.setCellFactory(tc -> {
            TableCell<Map<String, Object>, String> cell = new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item);
                    setAlignment(Pos.CENTER);
                    setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-family: 'Tahoma'; -fx-background-color: black;");
                }
            };
            return cell;
        });
        return col;
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }

    private void fetchSalesHistory() {
        new Thread(() -> {
            try {
                RestTemplate rest = new RestTemplate();
                SalesHistory[] sales = rest.getForObject("http://localhost:8080/sales/history", SalesHistory[].class);
                Platform.runLater(() -> {
                    // Convert SalesHistory[] to List<Map<String, Object>> if you want to reuse supplierTable
                    List<Map<String, Object>> salesList = Arrays.stream(sales).map(sale -> Map.<String, Object>of(
                        "product", sale.getProductName(),
                        "supplier", sale.getSupplierName(),
                        "location", "",
                        "quantity", sale.getQuantitySold(),
                        "unitValue", sale.getUnitPrice(),
                        "total", sale.getTotalPrice()
                    )).collect(Collectors.toList());
                    supplierTable.setItems(FXCollections.observableArrayList(salesList));
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert("Failed to fetch sales history"));
            }
        }).start();
    }
}