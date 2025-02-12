package com.translate.trans.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@Service
public class ExcelExportService {

    public void exportToExcel(HttpServletResponse response, List<String[]> data) throws IOException {
        // Đặt header cho file tải về
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=data.xlsx";
        response.setHeader(headerKey, headerValue);

        // Tạo workbook và sheet
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Data");

        // Tạo header
        Row headerRow = sheet.createRow(0);
        String[] headers = { "ID", "Name", "Email" };
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // Thêm dữ liệu vào sheet
        int rowCount = 1;
        for (String[] rowData : data) {
            Row row = sheet.createRow(rowCount++);
            for (int i = 0; i < rowData.length; i++) {
                row.createCell(i).setCellValue(rowData[i]);
            }
        }

        // Ghi dữ liệu ra response
        workbook.write(response.getOutputStream());
        workbook.close();
    }
}
