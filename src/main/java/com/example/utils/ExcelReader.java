package com.example.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Reads test data from an .xlsx file using Apache POI.
 *
 * Assumes the first row of the sheet is a HEADER row (column names) and every
 * row below it is a data record. Provides three read shapes:
 *   - readSheet(...)          -> List<Map<column, value>>  (named columns, most readable)
 *   - readSheetAsRows(...)    -> List<List<String>>        (raw grid, no headers)
 *   - readAsDataProvider(...) -> Object[][]                (drop straight into a @DataProvider)
 *
 * A DataFormatter is used so every cell comes back as the STRING the user sees
 * in Excel — this sidesteps the classic POI trap where a number like 1001 reads
 * back as "1001.0", or a date reads back as a serial number.
 */
public class ExcelReader {

    private ExcelReader() { }   // static-only utility; no instances

    /**
     * Reads a sheet into a list of row-maps keyed by the header names.
     * Example: [{ "username":"standard_user", "password":"secret_sauce" }, ...]
     */
    public static List<Map<String, String>> readSheet(String filePath, String sheetName) {
        List<Map<String, String>> rows = new ArrayList<>();
        DataFormatter fmt = new DataFormatter();

        // try-with-resources: the workbook + stream are closed automatically.
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new IllegalArgumentException("Sheet not found: " + sheetName);
            }

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                return rows;   // empty sheet
            }
            int colCount = headerRow.getLastCellNum();

            // Read the header names once.
            List<String> headers = new ArrayList<>();
            for (int c = 0; c < colCount; c++) {
                headers.add(fmt.formatCellValue(headerRow.getCell(c)).trim());
            }

            // Read every data row (row index 1 onward).
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null || isBlankRow(row, fmt, colCount)) {
                    continue;   // skip blank rows
                }
                Map<String, String> record = new LinkedHashMap<>();
                for (int c = 0; c < colCount; c++) {
                    Cell cell = row.getCell(c);
                    record.put(headers.get(c), fmt.formatCellValue(cell).trim());
                }
                rows.add(record);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read Excel file: " + filePath, e);
        }
        return rows;
    }

    /**
     * Reads a sheet as a raw grid of strings (no header handling).
     * Useful when you just want the cells positionally.
     */
    public static List<List<String>> readSheetAsRows(String filePath, String sheetName) {
        List<List<String>> grid = new ArrayList<>();
        DataFormatter fmt = new DataFormatter();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new IllegalArgumentException("Sheet not found: " + sheetName);
            }
            for (int r = 0; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                List<String> cells = new ArrayList<>();
                for (int c = 0; c < row.getLastCellNum(); c++) {
                    cells.add(fmt.formatCellValue(row.getCell(c)).trim());
                }
                grid.add(cells);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read Excel file: " + filePath, e);
        }
        return grid;
    }

    /**
     * Reads the DATA rows (skipping the header) into an Object[][] — the exact
     * shape a TestNG @DataProvider must return. Each inner array is one row's
     * cells in column order.
     */
    public static Object[][] readAsDataProvider(String filePath, String sheetName) {
        List<List<String>> grid = readSheetAsRows(filePath, sheetName);
        if (grid.size() <= 1) {
            return new Object[0][0];   // header only (or empty) => no data rows
        }
        List<List<String>> dataRows = grid.subList(1, grid.size());   // drop header
        Object[][] data = new Object[dataRows.size()][];
        for (int i = 0; i < dataRows.size(); i++) {
            data[i] = dataRows.get(i).toArray();
        }
        return data;
    }

    private static boolean isBlankRow(Row row, DataFormatter fmt, int colCount) {
        for (int c = 0; c < colCount; c++) {
            if (!fmt.formatCellValue(row.getCell(c)).trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}

