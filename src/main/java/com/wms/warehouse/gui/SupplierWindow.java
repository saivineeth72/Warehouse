package com.wms.warehouse.gui;

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
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Arrays;
import java.util.Locale;

public class SupplierWindow {

    private static final String SUPPLIERS_URL = "http://localhost:8080/suppliers";

    public static class Supplier {
        private String name;
        private String email;
        private String phone;
        private String contactInfo;
        private Long id;

        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
        public String getContactInfo() { return contactInfo; }
        public Long getId() { return id; }

        public void setName(String name) { this.name = name; }
        public void setEmail(String email) { this.email = email; }
        public void setPhone(String phone) { this.phone = phone; }
        public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }
        public void setId(Long id) { this.id = id; }
    }

    public void show() {
        Stage window = new Stage();
        window.setTitle("Supplier Directory");

        TableView<Supplier> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setStyle("-fx-background-color: black;");

        TableColumn<Supplier, String> nameCol = createStyledColumn("Name", "name");
        TableColumn<Supplier, String> emailCol = createStyledColumn("Email", "email");
        TableColumn<Supplier, String> phoneCol = createStyledColumn("Phone", "phone");

        TableColumn<Supplier, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setSortable(false);
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button updateBtn = createStyledButton("Update");
            private final Button deleteBtn = createStyledButton("Delete");
            private final HBox box = new HBox(10, updateBtn, deleteBtn);

            {
                updateBtn.setOnAction(e -> {
                    Supplier supplier = getTableView().getItems().get(getIndex());
                    showUpdateWindow(supplier);
                });

                deleteBtn.setOnAction(e -> {
                    Supplier supplier = getTableView().getItems().get(getIndex());
                    System.out.println("⏳ Attempting to delete supplier: " + supplier.getId() + " - " + supplier.getName());
                
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                        "Delete supplier \"" + supplier.getName() + "\"?",
                        ButtonType.YES, ButtonType.NO);
                
                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.YES) {
                            new Thread(() -> {
                                try {
                                    String url = SUPPLIERS_URL + "/" + supplier.getId();
                                    System.out.println("📡 Sending DELETE request to: " + url);
                                    new RestTemplate().delete(url);
                                    System.out.println("✅ Delete request completed.");
                
                                    Platform.runLater(() -> {
                                        getTableView().getItems().remove(supplier);
                                        showAlert(Alert.AlertType.INFORMATION, "Supplier deleted.");
                                    });
                                } catch (HttpClientErrorException.Conflict conflictEx) {
                                    Platform.runLater(() -> showAlert(Alert.AlertType.WARNING,
                                        "This supplier has products in the warehouse. Please remove all linked products before deleting."));
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "This supplier has products in the warehouse. Please remove all linked products before deleting."));
                                }
                            }).start();
                        }
                    });
                });

                box.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
                setAlignment(Pos.CENTER);
            }
        });

        table.getColumns().addAll(nameCol, emailCol, phoneCol, actionsCol);

        table.setRowFactory(tv -> {
            TableRow<Supplier> row = new TableRow<>();
            row.setStyle("-fx-background-color: black; -fx-border-color: #333; -fx-border-width: 0 0 1 0; -fx-border-style: solid;");
            row.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
                row.setStyle(isNowSelected ?
                        "-fx-background-color: #333; -fx-border-color: #333; -fx-border-width: 0 0 1 0; -fx-border-style: solid;" :
                        "-fx-background-color: black; -fx-border-color: #333; -fx-border-width: 0 0 1 0; -fx-border-style: solid;");
            });
            return row;
        });

        TextField searchField = new TextField();
        searchField.setPromptText("Search suppliers...");
        searchField.setPrefWidth(400);
        searchField.setStyle("-fx-background-color: #222; -fx-text-fill: white; -fx-border-color: white; -fx-font-size: 18px;");
        searchField.setPrefHeight(40);

        ObservableList<Supplier> masterList = FXCollections.observableArrayList();
        FilteredList<Supplier> filteredList = new FilteredList<>(masterList, p -> true);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String keyword = newVal.toLowerCase(Locale.ROOT);
            filteredList.setPredicate(s -> s.getName().toLowerCase().contains(keyword) ||
                    s.getEmail().toLowerCase().contains(keyword) ||
                    s.getPhone().toLowerCase().contains(keyword));
        });

        VBox layout = new VBox(15, new HBox(10, searchField), table);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: black;");
        VBox.setVgrow(table, Priority.ALWAYS);

        Scene scene = new Scene(layout, 900, 600);
        scene.getStylesheets().add(getClass().getResource("/dark-theme.css").toExternalForm());
        window.setScene(scene);
        window.show();

        new Thread(() -> {
            try {
                RestTemplate restTemplate = new RestTemplate();
                Supplier[] suppliers = restTemplate.getForObject(SUPPLIERS_URL, Supplier[].class);
                if (suppliers != null) {
                    Platform.runLater(() -> {
                        masterList.setAll(Arrays.asList(suppliers));
                        table.setItems(filteredList);
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Could not load suppliers"));
                e.printStackTrace();
            }
        }).start();
    }

    private <T> TableColumn<Supplier, T> createStyledColumn(String title, String property) {
        TableColumn<Supplier, T> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        col.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 24px; -fx-font-family: 'Tahoma'; -fx-font-weight: bold;");
        col.setCellFactory(tc -> {
            TableCell<Supplier, T> cell = new TableCell<>() {
                @Override
                protected void updateItem(T item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item == null ? "" : item.toString());
                    setTextFill(Color.WHITE);
                    setAlignment(Pos.CENTER);
                    setPadding(new Insets(10, 20, 10, 20));
                }
            };
            cell.setStyle("-fx-alignment: CENTER; -fx-font-size: 20px;");
            return cell;
        });
        return col;
    }

    private Button createStyledButton(String text) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Tahoma", 16));
        btn.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-border-color: white;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-border-color: white;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-border-color: white;"));
        btn.setPrefWidth(100);
        btn.setPrefHeight(40);
        return btn;
    }

    private void showUpdateWindow(Supplier supplier) {
        Stage updateStage = new Stage();
        updateStage.setTitle("Update Supplier");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(25);
        grid.setPadding(new Insets(40));
        grid.setStyle("-fx-background-color: black;");

        Font font = Font.font("Tahoma", 18);

        TextField nameField = createTextField("Name", grid, 0, font, supplier.getName());
        TextField emailField = createTextField("Email", grid, 1, font, supplier.getEmail());
        TextField phoneField = createTextField("Phone", grid, 2, font, supplier.getPhone());
        TextField contactField = createTextField("Contact Info", grid, 3, font, supplier.getContactInfo());

        Button saveButton = createStyledButton("Save");
        saveButton.setOnAction(ev -> {
            supplier.setName(nameField.getText());
            supplier.setEmail(emailField.getText());
            supplier.setPhone(phoneField.getText());
            supplier.setContactInfo(contactField.getText());

            try {
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.put(SUPPLIERS_URL + "/" + supplier.getId(), supplier);
                updateStage.close();
                showAlert(Alert.AlertType.INFORMATION, "Supplier updated successfully!");
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Failed to update supplier.");
            }
        });

        grid.add(saveButton, 1, 5);

        Scene scene = new Scene(grid, 500, 400);
        updateStage.setScene(scene);
        updateStage.show();
    }

    private TextField createTextField(String label, GridPane grid, int row, Font font, String value) {
        Label l = new Label(label + ":");
        l.setTextFill(Color.WHITE);
        l.setFont(font);
        TextField tf = new TextField(value);
        tf.setFont(font);
        grid.add(l, 0, row);
        grid.add(tf, 1, row);
        return tf;
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type, message);
        alert.showAndWait();
    }
}