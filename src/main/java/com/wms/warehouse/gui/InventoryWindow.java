package com.wms.warehouse.gui;

import com.wms.warehouse.model.Product;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Locale;

public class InventoryWindow {

    private static final String PRODUCTS_URL = "http://localhost:8080/products";

    public void show() {
        Stage window = new Stage();
        window.setTitle("Product Inventory");

        TableView<Product> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setStyle("-fx-background-color: black;");
        table.setPadding(new Insets(20));

        // === Table Columns ===
        TableColumn<Product, Integer> idCol = createStyledColumn("ID", "id");
        TableColumn<Product, String> nameCol = createStyledColumn("Name", "name");
        TableColumn<Product, Integer> quantityCol = createStyledColumn("Quantity", "quantity");
        TableColumn<Product, String> brandCol = createStyledColumn("Brand", "brand");
        TableColumn<Product, String> locationCol = createStyledColumn("Location", "location");

        TableColumn<Product, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setSortable(false);
        actionsCol.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-family: 'Arial'; -fx-font-weight: bold; -fx-alignment: CENTER;");
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button updateBtn = createStyledButton("Update");
            private final Button deleteBtn = createStyledButton("Delete");
            private final Button detailsBtn = createStyledButton("More Details");
            private final HBox box = new HBox(10, updateBtn, deleteBtn, detailsBtn);

            {
                box.setAlignment(Pos.CENTER);
                box.setPadding(new Insets(5));
                box.setStyle("-fx-border-width: 0;");

                updateBtn.setOnAction(e -> {
                    Product selected = getTableView().getItems().get(getIndex());
                    new UpdateProductForm(selected).show();
                });

                deleteBtn.setOnAction(e -> {
                    Product selected = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete " + selected.getName() + "?", ButtonType.YES, ButtonType.NO);
                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.YES) {
                            new Thread(() -> {
                                try {
                                    new RestTemplate().delete(PRODUCTS_URL + "/" + selected.getId());
                                    Platform.runLater(() -> {
                                        getTableView().getItems().remove(selected);
                                        showAlert("Product deleted.", Alert.AlertType.INFORMATION);
                                    });
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    Platform.runLater(() -> showAlert("Failed to delete product.", Alert.AlertType.ERROR));
                                }
                            }).start();
                        }
                    });
                });

                detailsBtn.setOnAction(e -> {
                    Product selected = getTableView().getItems().get(getIndex());
                    new ProductDetailsWindow(selected).show();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(box);
                }
                setAlignment(Pos.CENTER);
            }
        });

        table.getColumns().addAll(idCol, nameCol, quantityCol, brandCol, locationCol, actionsCol);

        table.setRowFactory(tv -> {
            TableRow<Product> row = new TableRow<>();
            row.setStyle("-fx-background-color: black; -fx-border-color: #333; -fx-border-width: 0 0 1 0; -fx-border-style: solid;");
            row.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
                row.setStyle(isNowSelected ? 
                    "-fx-background-color: #333; -fx-border-color: #333; -fx-border-width: 0 0 1 0; -fx-border-style: solid;" : 
                    "-fx-background-color: black; -fx-border-color: #333; -fx-border-width: 0 0 1 0; -fx-border-style: solid;");
            });
            return row;
        });

        // === Search Bar ===
        TextField searchField = new TextField();
        searchField.setPromptText("Search...");
        searchField.setStyle("-fx-background-color: #222; -fx-text-fill: white; -fx-border-color: white;");
        searchField.setPrefWidth(400);

        HBox searchBox = new HBox(searchField);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setPadding(new Insets(15, 15, 10, 15));
        searchBox.setSpacing(10);

        // === Filter and Load ===
        ObservableList<Product> masterList = FXCollections.observableArrayList();
        FilteredList<Product> filteredList = new FilteredList<>(masterList, p -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String keyword = newVal.toLowerCase(Locale.ROOT);
            filteredList.setPredicate(product ->
                    product.getName().toLowerCase().contains(keyword) ||
                            product.getBrand().toLowerCase().contains(keyword) ||
                            product.getLocation().toLowerCase().contains(keyword)
            );
        });

        try {
            Product[] products = new RestTemplate().getForObject(PRODUCTS_URL, Product[].class);
            if (products != null) masterList.setAll(Arrays.asList(products));
        } catch (Exception e) {
            e.printStackTrace();
        }

        table.setItems(filteredList);

        // === Layout ===
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: black;");
        VBox.setVgrow(table, Priority.ALWAYS);
        layout.getChildren().addAll(searchBox, table);

        Scene scene = new Scene(layout, 1150, 650);
        scene.getStylesheets().add(getClass().getResource("/dark-theme.css").toExternalForm());
        window.setScene(scene);
        window.show();
    }

    private <T> TableColumn<Product, T> createStyledColumn(String title, String property) {
        TableColumn<Product, T> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        col.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 20px; -fx-font-family: 'Tahoma'; -fx-font-weight: bold; -fx-alignment: CENTER;");
        col.setCellFactory(tc -> {
            TableCell<Product, T> cell = new TableCell<>() {
                @Override
                protected void updateItem(T item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                    } else {
                        setText(item == null ? "" : item.toString());
                        setAlignment(Pos.CENTER);
                    }
                }
            };
            cell.setStyle("-fx-alignment: CENTER; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-family: 'Tahoma';");
            return cell;
        });
        return col;
    }

    private Button createStyledButton(String text) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Arial", 14));
        btn.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-border-color: white;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-border-color: white;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-border-color: white;"));
        btn.setPrefWidth(100);
        return btn;
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type, message);
        alert.showAndWait();
    }
}