package com.bookstore.frontend.controller;

import com.bookstore.frontend.model.dto.Response.RevenueYearResponse;
import com.bookstore.frontend.model.dto.Response.RevenueSummaryResponse;
import com.bookstore.frontend.service.api.RevenueApiService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import java.time.LocalDate;

public class RevenueReportController {

    @FXML private BarChart<String, Number> revenueChart;
    @FXML private NumberAxis yAxis;
    @FXML private CategoryAxis xAxis; // Đã khớp với fx:id="xAxis" trong FXML
    @FXML private ComboBox<Integer> yearPicker;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Button btnLoad;
    @FXML private Label lblTotalRevenue;
    @FXML private Label lblTotalProfit;
    @FXML private Label lblEmpty;

    @FXML
    public void initialize() {
        // 1. Khởi tạo năm cho ComboBox
        int currentYear = LocalDate.now().getYear();
        for (int i = currentYear; i >= 2020; i--) {
            yearPicker.getItems().add(i);
        }
        yearPicker.setValue(currentYear);

        // 2. Cấu hình trục
        yAxis.setForceZeroInRange(true);
        xAxis.setAnimated(false); // Quan trọng để tránh nhảy nhãn
        xAxis.setTickLabelGap(10);

        revenueChart.setAnimated(true);

        // 3. Load dữ liệu lần đầu
        handleLoadReport();
    }

    @FXML
    private void handleLoadReport() {
        Integer year = yearPicker.getValue();
        if (year == null) return;

        setLoading(true);

        RevenueApiService.getInstance().getRevenueByYear(year)
                .thenAccept(data -> Platform.runLater(() -> {
                    setLoading(false);
                    updateChart(data);
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        setLoading(false);
                        System.err.println("Load thất bại: " + ex.getMessage());
                    });
                    return null;
                });
    }

    private void updateChart(RevenueYearResponse data) {
        boolean hasData = data != null && data.months() != null && !data.months().isEmpty();

        lblEmpty.setVisible(!hasData);
        revenueChart.setVisible(hasData);

        if (!hasData) return;

        revenueChart.getData().clear();
        XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
        revenueSeries.setName("Doanh thu");

        XYChart.Series<String, Number> profitSeries = new XYChart.Series<>();
        profitSeries.setName("Lợi nhuận");

        double tRev = 0, tProf = 0;

        // Vòng lặp cố định 12 tháng để trục X luôn đủ T1-T12
        for (int i = 1; i <= 12; i++) {
            final int month = i;
            RevenueSummaryResponse mData = data.months().stream()
                    .filter(x -> x.month() == month).findFirst().orElse(null);

            double rev = (mData != null) ? mData.revenue().doubleValue() : 0.0;
            double prof = (mData != null) ? mData.profit().doubleValue() : 0.0;

            tRev += rev;
            tProf += prof;

            revenueSeries.getData().add(new XYChart.Data<>("T" + i, rev));
            profitSeries.getData().add(new XYChart.Data<>("T" + i, prof));
        }

        lblTotalRevenue.setText(String.format("%,.0f VND", tRev));
        lblTotalProfit.setText(String.format("%,.0f VND", tProf));
        lblTotalProfit.setStyle(tProf < 0 ? "-fx-text-fill: #ff5555;" : "-fx-text-fill: #00bcd4;");

        revenueChart.getData().addAll(revenueSeries, profitSeries);
        Platform.runLater(() -> revenueChart.requestLayout());
    }

    private void setLoading(boolean isLoading) {
        if (loadingIndicator != null) loadingIndicator.setVisible(isLoading);
        if (btnLoad != null) btnLoad.setDisable(isLoading);
    }
}