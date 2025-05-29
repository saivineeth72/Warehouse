package com.wms.warehouse.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.sql.*;
import java.text.SimpleDateFormat;

public class ExcelToPostgresImporter {

    private static final String EXCEL_PATH = "data/historical_supplier_performance.xlsx";
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/wmsdb";
    private static final String DB_USER = "saivineethpinnoju";
    private static final String DB_PASS = "wmspass";

    public static void main(String[] args) {
        System.out.println("✅ Main method working");
        org.apache.poi.util.IOUtils.setByteArrayMaxOverride(300_000_000);
        try (
                Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                FileInputStream fis = new FileInputStream(new File(EXCEL_PATH));
                Workbook workbook = new XSSFWorkbook(fis)
        ) {
            Sheet sheet = workbook.getSheetAt(0);
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO supplier_performance_history (product_name, order_date, received_date, supplier, order_demand) VALUES (?, ?, ?, ?, ?)"
            );

            SimpleDateFormat parser = new SimpleDateFormat("MM/dd/yyyy");

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                // --- Product Name ---
                Cell productNameCell = row.getCell(0);
                if (productNameCell == null || productNameCell.getCellType() != CellType.STRING) {
                    System.out.println("Skipping row " + i + ": invalid product name");
                    continue;
                }
                String productName = productNameCell.getStringCellValue().trim();

                // --- Order Date ---
                Cell orderDateCell = row.getCell(1);
                Date orderDate;
                try {
                    if (orderDateCell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(orderDateCell)) {
                        orderDate = new Date(orderDateCell.getDateCellValue().getTime());
                    } else {
                        String dateStr = orderDateCell.getStringCellValue().trim();
                        orderDate = new Date(parser.parse(dateStr).getTime());
                    }
                } catch (Exception e) {
                    System.out.println("Skipping row " + i + ": invalid order date");
                    continue;
                }

                // --- Received Date ---
                Cell receivedDateCell = row.getCell(2);
                Date receivedDate;
                try {
                    if (receivedDateCell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(receivedDateCell)) {
                        receivedDate = new Date(receivedDateCell.getDateCellValue().getTime());
                    } else {
                        String dateStr = receivedDateCell.getStringCellValue().trim();
                        receivedDate = new Date(parser.parse(dateStr).getTime());
                    }
                } catch (Exception e) {
                    System.out.println("Skipping row " + i + ": invalid received date");
                    continue;
                }

                // --- Supplier ---
                Cell supplierCell = row.getCell(3);
                if (supplierCell == null || supplierCell.getCellType() != CellType.STRING) {
                    System.out.println("Skipping row " + i + ": invalid supplier");
                    continue;
                }
                String supplier = supplierCell.getStringCellValue().trim();

                // --- Order Demand ---
                Cell demandCell = row.getCell(4);
                if (demandCell == null || demandCell.getCellType() != CellType.NUMERIC) {
                    System.out.println("Skipping row " + i + ": invalid order demand");
                    continue;
                }
                int orderDemand = (int) demandCell.getNumericCellValue();

                // --- Add to batch ---
                stmt.setString(1, productName);
                stmt.setDate(2, orderDate);
                stmt.setDate(3, receivedDate);
                stmt.setString(4, supplier);
                stmt.setInt(5, orderDemand);
                stmt.addBatch();

                if (i % 500 == 0) {
                    stmt.executeBatch();
                }
            }

            stmt.executeBatch();
            System.out.println("✅ Data import completed.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}