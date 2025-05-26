package com.wms.warehouse.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.sql.*;
import java.text.SimpleDateFormat;

public class ExcelToPostgresImporter {

    private static final String EXCEL_PATH = "data/historical_warehouse_demand.xlsx";
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
                    "INSERT INTO product_demand_history (product_code, demand_date, quantity) VALUES (?, ?, ?)"
            );

            SimpleDateFormat parser = new SimpleDateFormat("MM/dd/yy");

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
            
                // --- Product Code ---
                Cell productCodeCell = row.getCell(0);
                String productCode;
                if (productCodeCell == null) continue;
                if (productCodeCell.getCellType() == CellType.STRING) {
                    productCode = productCodeCell.getStringCellValue().trim();
                } else if (productCodeCell.getCellType() == CellType.NUMERIC) {
                    productCode = String.valueOf((long) productCodeCell.getNumericCellValue()).trim();
                } else {
                    System.out.println("Skipping row " + i + ": invalid product code format");
                    continue;
                }
            
                Cell dateCell = row.getCell(2);
                Date sqlDate;

                try {
                    if (dateCell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(dateCell)) {
                        java.util.Date parsed = dateCell.getDateCellValue();
                        sqlDate = new Date(parsed.getTime());
                    } else if (dateCell.getCellType() == CellType.STRING) {
                        String dateStr = dateCell.getStringCellValue().trim();
                        java.util.Date parsed = parser.parse(dateStr);
                        sqlDate = new Date(parsed.getTime());
                    } else {
                        throw new Exception("Invalid cell type");
                    }
                } catch (Exception e) {
                    System.out.println("Skipping row " + i + ": invalid date format");
                    continue;
                }
            
                // --- Demand ---
                Cell demandCell = row.getCell(3);
                if (demandCell == null || demandCell.getCellType() != CellType.NUMERIC) {
                    System.out.println("Skipping row " + i + ": invalid demand");
                    continue;
                }
                int demand = (int) demandCell.getNumericCellValue();
            
                // --- Add to batch ---
                stmt.setString(1, productCode);
                stmt.setDate(2, sqlDate);
                stmt.setInt(3, demand);
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