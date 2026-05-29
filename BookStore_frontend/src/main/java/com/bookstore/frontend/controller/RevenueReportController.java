package com.bookstore.frontend.controller;

import com.bookstore.frontend.model.dto.Response.*;
import com.bookstore.frontend.service.api.RevenueApiService;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;


public class RevenueReportController {
    @FXML private BarChart<String, Number> barChart;
    @FXML private LineChart<String, Number> lineChart;
    @FXML private TextField yearInput;
    @FXML private ComboBox<String> chartTypePicker;
    @FXML private TableView<TopProductResponse> tblTopProducts;
    @FXML private TableColumn<TopProductResponse, String> colBookName, colQuantity;
    @FXML private Label lblTotalRevenue, lblTotalProfit, lblTotalOrders;
    @FXML private ProgressIndicator loadingIndicator;

    @FXML
    public void initialize() {
        yearInput.setText(String.valueOf(LocalDate.now().getYear()));
        chartTypePicker.getItems().addAll("Biểu đồ cột", "Biểu đồ đường");
        chartTypePicker.setValue("Biểu đồ cột");

        // Khi người dùng chọn lại loại biểu đồ, chỉ cần thay đổi hiển thị
        chartTypePicker.setOnAction(e -> updateChartVisibility());

        colBookName.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().bookTitle()));
        colQuantity.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(String.valueOf(data.getValue().soldQuantity())));
    }

    private void updateChartVisibility() {
        boolean isBar = "Biểu đồ cột".equals(chartTypePicker.getValue());
        barChart.setVisible(isBar);
        lineChart.setVisible(!isBar);
        // Ép layout lại để JavaFX vẽ lại đúng kích thước khi đổi chart
        barChart.requestLayout();
        lineChart.requestLayout();
    }

    @FXML
    public void handleLoadReport() {
        if (!yearInput.getText().matches("\\d{4}")) return;
        int year = Integer.parseInt(yearInput.getText());
        loadingIndicator.setVisible(true);

        // Mặc định gọi theo tháng
        RevenueApiService.getInstance().getRevenueByYear(year)
                .thenAccept(data -> Platform.runLater(() -> {
                    if (data != null && data.months() != null) {
                        List<DataPoint> points = data.months().stream()
                                .map(m -> new DataPoint("T" + m.month(), m.revenue(), m.importCost()))
                                .collect(Collectors.toList());
                        updateUI(data, points);
                    }
                    loadingIndicator.setVisible(false);
                }));
    }

    @FXML
    public void handleExportExcel() {
        if (!yearInput.getText().matches("\\d{4}")) return;
        int year = Integer.parseInt(yearInput.getText());

        loadingIndicator.setVisible(true);

        // Sửa đoạn code ở dòng 77-83 trong RevenueReportController.java
        RevenueApiService.getInstance().exportRevenueToExcel(year)
                .thenAccept(bytes -> Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);

                    // Vì 'bytes' ở đây chính là dữ liệu file, ta không cần kiểm tra statusCode nữa
                    // (Nếu có lỗi từ Server, nó đã ném ra Exception rồi)

                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setTitle("Lưu báo cáo doanh thu");
                    fileChooser.setInitialFileName("revenue-report-" + year + ".xlsx");
                    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel", "*.xlsx"));

                    File targetFile = fileChooser.showSaveDialog(barChart.getScene().getWindow());

                    if (targetFile != null) {
                        try {
                            java.nio.file.Files.write(targetFile.toPath(), bytes);
                            new Alert(Alert.AlertType.INFORMATION, "Xuất file thành công!").show();
                        } catch (Exception e) {
                            new Alert(Alert.AlertType.ERROR, "Không thể lưu file: " + e.getMessage()).show();
                        }
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        loadingIndicator.setVisible(false);
                        // In ra lỗi chi tiết nếu server trả về lỗi 400/500
                        new Alert(Alert.AlertType.ERROR, "Xuất Excel thất bại: " + ex.getMessage()).show();
                    });
                    return null;
                });
    }


    private void updateUI(RevenueYearResponse data, List<DataPoint> points) {
        lblTotalRevenue.setText(String.format("%,.0f VNĐ", data.revenue()));
        lblTotalProfit.setText(String.format("%,.0f VNĐ", data.profit()));
        lblTotalOrders.setText(String.valueOf(data.orderCount()));

        tblTopProducts.setItems(data.topProducts() != null ? FXCollections.observableArrayList(data.topProducts()) : FXCollections.observableArrayList());

        // VẼ CẢ 2 BIỂU ĐỒ CÙNG LÚC ĐỂ DỮ LIỆU LUÔN SẴN SÀNG
        renderCharts(points);
    }

    private void renderCharts(List<DataPoint> points) {
        barChart.getData().clear();
        lineChart.getData().clear();

        // Series cho cột
        XYChart.Series<String, Number> barRev = new XYChart.Series<>(); barRev.setName("Doanh thu");
        XYChart.Series<String, Number> barCost = new XYChart.Series<>(); barCost.setName("Chi phí");

        // Series cho đường
        XYChart.Series<String, Number> lineRev = new XYChart.Series<>(); lineRev.setName("Doanh thu");
        XYChart.Series<String, Number> lineCost = new XYChart.Series<>(); lineCost.setName("Chi phí");

        for (DataPoint p : points) {
            barRev.getData().add(new XYChart.Data<>(p.label(), p.rev()));
            barCost.getData().add(new XYChart.Data<>(p.label(), p.cost()));

            lineRev.getData().add(new XYChart.Data<>(p.label(), p.rev()));
            lineCost.getData().add(new XYChart.Data<>(p.label(), p.cost()));
        }

        barChart.getData().addAll(barRev, barCost);
        lineChart.getData().addAll(lineRev, lineCost);

        updateChartVisibility();
    }

    private record DataPoint(String label, java.math.BigDecimal rev, java.math.BigDecimal cost) {}
}