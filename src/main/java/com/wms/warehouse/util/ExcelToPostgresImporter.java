package com.wms.warehouse.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.sql.*;
import java.text.SimpleDateFormat;

public class ExcelToPostgresImporter {

    private static final String EXCEL_PATH = "data/historical_supplier_performance_with_brand_price.xlsx";
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
                "INSERT INTO sales_history (product_name, quantity_sold, unit_price, total_price, supplier_name, sale_date, brand) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)"
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

                // --- Sale Date ---
                Cell saleDateCell = row.getCell(1); // 'Date' column
                Date saleDate;
                try {
                    if (saleDateCell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(saleDateCell)) {
                        saleDate = new Date(saleDateCell.getDateCellValue().getTime());
                    } else {
                        String dateStr = saleDateCell.getStringCellValue().trim();
                        saleDate = new Date(parser.parse(dateStr).getTime());
                    }
                } catch (Exception e) {
                    System.out.println("Skipping row " + i + ": invalid sale date");
                    continue;
                }

                // --- Supplier Name ---
                Cell supplierCell = row.getCell(3);
                if (supplierCell == null || supplierCell.getCellType() != CellType.STRING) {
                    System.out.println("Skipping row " + i + ": invalid supplier");
                    continue;
                }
                String supplierName = supplierCell.getStringCellValue().trim();

                // --- Quantity Sold ---
                Cell quantityCell = row.getCell(4);
                if (quantityCell == null || quantityCell.getCellType() != CellType.NUMERIC) {
                    System.out.println("Skipping row " + i + ": invalid quantity");
                    continue;
                }
                int quantitySold = (int) quantityCell.getNumericCellValue();

                // --- Brand ---
                Cell brandCell = row.getCell(5);
                if (brandCell == null || brandCell.getCellType() != CellType.STRING) {
                    System.out.println("Skipping row " + i + ": invalid brand");
                    continue;
                }
                String brand = brandCell.getStringCellValue().trim();

                // --- Unit Price ---
                Cell unitPriceCell = row.getCell(6);
                if (unitPriceCell == null || unitPriceCell.getCellType() != CellType.NUMERIC) {
                    System.out.println("Skipping row " + i + ": invalid unit price");
                    continue;
                }
                int unitPrice = (int) unitPriceCell.getNumericCellValue();

                // --- Total Price ---
                double totalPrice = quantitySold * unitPrice;

                // --- Add to batch ---
                stmt.setString(1, productName);
                stmt.setInt(2, quantitySold);
                stmt.setInt(3, unitPrice);
                stmt.setDouble(4, totalPrice);
                stmt.setString(5, supplierName);
                stmt.setDate(6, saleDate);
                stmt.setString(7, brand);
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