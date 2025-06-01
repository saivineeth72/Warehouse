package com.wms.warehouse.gui;

import javafx.application.Application;
import javafx.stage.Stage;

public class EmployeeDashboard extends Application {

    @Override
    public void start(Stage stage) {
        // Launch AdminDashboard with role 'employee' to hide Suppliers button
        new AdminDashboard("employee").start(new Stage());
        stage.close();
    }
} 