package com.wms.warehouse.gui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.stream.Collectors;

public class SupplierWindow {

    private static final String SUPPLIERS_URL = "http://localhost:8080/suppliers";

    public static class Supplier {
        private String name;
        private String email;
        private String phone;

        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
    }

    public void show() {
        Stage window = new Stage();
        window.setTitle("Suppliers");

        TableView<Supplier> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Supplier, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Supplier, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<Supplier, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));

        TableColumn<Supplier, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button updateBtn = new Button("Update");
            {
                updateBtn.setOnAction(e -> {
                    Supplier supplier = getTableView().getItems().get(getIndex());
                    // Placeholder: Add update logic here
                    System.out.println("Updating: " + supplier.getName());
                });
                updateBtn.setFont(new Font(14));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(updateBtn);
                }
            }
        });

        table.getColumns().addAll(nameCol, emailCol, phoneCol, actionCol);

        TextField searchField = new TextField();
        searchField.setPromptText("Search suppliers...");
        searchField.setPrefWidth(300);

        ObservableList<Supplier> allSuppliers = FXCollections.observableArrayList();
        ObservableList<Supplier> filteredSuppliers = FXCollections.observableArrayList();

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String query = newVal.toLowerCase();
            filteredSuppliers.setAll(
                    allSuppliers.stream()
                            .filter(s -> s.getName().toLowerCase().contains(query) ||
                                    s.getEmail().toLowerCase().contains(query) ||
                                    s.getPhone().toLowerCase().contains(query))
                            .collect(Collectors.toList())
            );
        });

        VBox layout = new VBox(10, new HBox(10, searchField), table);
        layout.setPadding(new Insets(20));

        BorderPane root = new BorderPane(layout);
        Scene scene = new Scene(root, 700, 500);
        window.setScene(scene);
        window.show();

        new Thread(() -> {
            try {
                RestTemplate restTemplate = new RestTemplate();
                Supplier[] suppliers = restTemplate.getForObject(SUPPLIERS_URL, Supplier[].class);
                if (suppliers != null) {
                    Platform.runLater(() -> {
                        allSuppliers.addAll(Arrays.asList(suppliers));
                        filteredSuppliers.setAll(allSuppliers);
                        table.setItems(filteredSuppliers);
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Could not load suppliers");
                    alert.showAndWait();
                });
                e.printStackTrace();
            }
        }).start();
    }
}
