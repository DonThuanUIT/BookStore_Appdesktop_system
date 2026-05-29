package com.bookstore.backend.service;

import com.bookstore.backend.dto.response.RevenueSummaryResponse;
import com.bookstore.backend.dto.response.RevenueYearResponse;
import com.bookstore.backend.dto.response.TopProductResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;

@Service
public class RevenueExportService {

    public byte[] exportRevenueToExcel(RevenueYearResponse data) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Revenue " + data.year());

            int rowIdx = 0;
            rowIdx = writeTitle(sheet, rowIdx, "BÁO CÁO DOANH THU NĂM " + data.year());
            rowIdx = writeTotals(sheet, rowIdx, data);

            rowIdx = writeMonthlyHeader(sheet, rowIdx);

            if (data.months() != null) {
                for (RevenueSummaryResponse m : data.months()) {
                    rowIdx = writeMonthlyRow(sheet, rowIdx, m);
                }
            }

            rowIdx += 1;
            rowIdx = writeTopProductsHeader(sheet, rowIdx);

            if (data.topProducts() != null) {
                for (TopProductResponse p : data.topProducts()) {
                    Row row = sheet.createRow(rowIdx++);
                    int col = 0;
                    createCell(row, col++, p.bookTitle());
                    createCell(row, col, p.soldQuantity());
                }
            }

            for (int i = 0; i < 10; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to export revenue excel", e);
        }
    }

    private int writeTitle(Sheet sheet, int rowIdx, String title) {
        Row row = sheet.createRow(rowIdx++);
        createCell(row, 0, title);
        return rowIdx;
    }

    private int writeTotals(Sheet sheet, int rowIdx, RevenueYearResponse data) {
        Row r1 = sheet.createRow(rowIdx++);
        createCell(r1, 0, "Tổng Doanh Thu");
        createCell(r1, 1, money(data.revenue()));

        Row r2 = sheet.createRow(rowIdx++);
        createCell(r2, 0, "Tổng Lợi Nhuận");
        createCell(r2, 1, money(data.profit()));

        Row r3 = sheet.createRow(rowIdx++);
        createCell(r3, 0, "Tổng Đơn Hàng");
        createCell(r3, 1, data.orderCount());

        Row r4 = sheet.createRow(rowIdx++);
        createCell(r4, 0, "Tổng Số Nhập");
        createCell(r4, 1, data.importCount());

        return rowIdx;
    }

    private int writeMonthlyHeader(Sheet sheet, int rowIdx) {
        Row header = sheet.createRow(rowIdx++);
        int col = 0;
        createCell(header, col++, "Tháng");
        createCell(header, col++, "Doanh Thu");
        createCell(header, col++, "Chi Phí Nhập");
        createCell(header, col++, "Lợi Nhuận");
        createCell(header, col++, "Số Đơn Hàng");
        createCell(header, col++, "Số Lần Nhập");

        return rowIdx;
    }

    private int writeMonthlyRow(Sheet sheet, int rowIdx, RevenueSummaryResponse m) {
        Row row = sheet.createRow(rowIdx++);
        int col = 0;
        createCell(row, col++, m.month());
        createCell(row, col++, money(m.revenue()));
        createCell(row, col++, money(m.importCost()));
        createCell(row, col++, money(m.profit()));
        createCell(row, col++, m.orderCount());
        createCell(row, col, m.importCount());
        return rowIdx;
    }

    private int writeTopProductsHeader(Sheet sheet, int rowIdx) {
        Row header = sheet.createRow(rowIdx++);
        createCell(header, 0, "Top Sản Phẩm");
        return rowIdx;
    }

    private void createCell(Row row, int col, Object value) {
        Cell cell = row.createCell(col);
        if (value == null) {
            cell.setBlank();
            return;
        }
        if (value instanceof Number n) {
            if (value instanceof Long l) cell.setCellValue(l);
            else if (value instanceof Integer i) cell.setCellValue(i);
            else if (value instanceof Double d) cell.setCellValue(d);
            else if (value instanceof BigDecimal bd) cell.setCellValue(bd.doubleValue());
            else cell.setCellValue(n.doubleValue());
        } else {
            cell.setCellValue(String.valueOf(value));
        }
    }

    private String money(BigDecimal v) {
        if (v == null) return "0";
        return v.toPlainString();
    }
}

