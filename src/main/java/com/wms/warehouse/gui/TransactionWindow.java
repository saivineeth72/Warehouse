package com.wms.warehouse.gui;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.wms.warehouse.controller.SupplierController;


public class TransactionWindow {

    private static final String TRANSACTIONS_URL = "http://localhost:8080/transactions";
    private static final String SUPPLIERS_URL = "http://localhost:8080/suppliers";

    private static final ObjectMapper mapper = new ObjectMapper();
    private static java.util.Map<Long, String> supplierIdToName = new java.util.HashMap<>();

    public static class Transaction {
        private String actionType;
        private LocalDateTime timestamp;
        private String oldData;
        private String newData;

        public String getActionType() { return actionType; }
        public void setActionType(String actionType) { this.actionType = actionType; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        public String getOldData() { return oldData; }

        public String getNewData() { return newData; }


        public String getFormattedTimestamp() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy HH:mm", Locale.ENGLISH);
            return timestamp.format(formatter);
        }

        public String getDetails() {
            long supplierId = Long.parseLong(parseFieldFromJson(oldData, "supplier_id"));
            String supplierName = supplierIdToName.getOrDefault(supplierId, "Unknown");
            if(actionType.equalsIgnoreCase("insert")) {
                return parseFieldFromJson(newData, "brand") + "\n" + parseFieldFromJson(newData, "name") + "\nSupplier: " + supplierName;
            } else {
                return parseFieldFromJson(oldData, "brand") + "\n" + parseFieldFromJson(oldData, "name") + "\nSupplier: " + supplierName;
            }
        }

        public int getQuantityDifference() {
            return Integer.parseInt(parseFieldFromJson(newData, "quantity")) - Integer.parseInt(parseFieldFromJson(oldData, "quantity"));
        }

        public String getDisplayActionType() {
            return actionType.equalsIgnoreCase("insert") ? "BOUGHT" : "SOLD";
        }
    }

    public static class Supplier {
        private Long id;
        private String name;
        public Long getId() { return id; }
        public String getName() { return name; }
    }

    public void show() {
        Stage window = new Stage();
        window.setTitle("Transaction History");

        TableView<Transaction> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setStyle("-fx-background-color: black;");
        table.setPadding(new Insets(20));

        TableColumn<Transaction, String> actionCol = new TableColumn<>("Action");
        actionCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getDisplayActionType()));
        styleColumn(actionCol, Pos.CENTER);

        TableColumn<Transaction, String> timeCol = new TableColumn<>("Timestamp");
        timeCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getFormattedTimestamp()));
        styleColumn(timeCol, Pos.CENTER_LEFT);

        TableColumn<Transaction, String> detailsCol = new TableColumn<>("Details");
        detailsCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getDetails()));
        styleColumn(detailsCol, Pos.CENTER_LEFT);

        TableColumn<Transaction, Integer> diffCol = new TableColumn<>("Qty Diff");
        diffCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getQuantityDifference()));
        styleColumn(diffCol, Pos.CENTER);

        table.getColumns().addAll(actionCol, timeCol, detailsCol, diffCol);

        table.setFixedCellSize(100);
        table.prefHeightProperty().bind(table.fixedCellSizeProperty().multiply(table.getItems().size()).add(60));

        table.setRowFactory(tv -> {
            TableRow<Transaction> row = new TableRow<>();
            row.setStyle("-fx-background-color: black; -fx-border-color: white; -fx-border-width: 0 0 1 0; -fx-border-style: solid;");
            row.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
                row.setStyle(isNowSelected ?
                    "-fx-background-color: #333; -fx-border-color: #333; -fx-border-width: 0 0 1 0; -fx-border-style: solid;" :
                    "-fx-background-color: black; -fx-border-color: #333; -fx-border-width: 0 0 1 0; -fx-border-style: solid;");
            });
            return row;
        });

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: black;");
        VBox.setVgrow(table, Priority.ALWAYS);
        layout.getChildren().addAll(new Label("Transactions"), table);

        Scene scene = new Scene(layout, 900, 500);
        scene.getStylesheets().add(getClass().getResource("/dark-theme.css").toExternalForm());
        window.setScene(scene);
        window.show();

        new Thread(() -> {
            try {
                RestTemplate restTemplate = new RestTemplate();
                Supplier[] suppliers = restTemplate.getForObject(SUPPLIERS_URL, Supplier[].class);
                for (Supplier s : suppliers) {
                    supplierIdToName.put(s.getId(), s.getName());
                }
        
                // Now fetch transactions
                Transaction[] transactions = restTemplate.getForObject(TRANSACTIONS_URL, Transaction[].class);
                Platform.runLater(() -> table.setItems(FXCollections.observableArrayList(transactions)));
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> table.setItems(FXCollections.observableArrayList()));
            }
        }).start();
    }

    private <T> void styleColumn(TableColumn<Transaction, T> col, Pos alignment) {
        col.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 20px; -fx-font-family: 'Tahoma'; -fx-font-weight: bold; -fx-alignment: CENTER;");
        col.setCellFactory(tc -> {
            TableCell<Transaction, T> cell = new TableCell<>() {
                @Override
                protected void updateItem(T item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                    } else {
                        setText(item == null ? "" : item.toString());
                        setAlignment(alignment);
                        setPadding(new Insets(0, 20, 0, 20));
                    }
                }
            };
            cell.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-family: 'Tahoma';");
            return cell;
        });
    }

    private static String parseFieldFromJson(String json, String fieldName) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(json);
            return node.has(fieldName) ? node.get(fieldName).asText() : "1";
        } catch (Exception e) {
            return "1";
        }
    }
} 
