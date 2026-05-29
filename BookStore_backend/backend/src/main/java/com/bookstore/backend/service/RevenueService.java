package com.bookstore.backend.service;

import com.bookstore.backend.dto.response.RevenueSummaryResponse;
import com.bookstore.backend.dto.response.RevenueYearResponse;
import com.bookstore.backend.dto.response.TopProductResponse;
import com.bookstore.backend.exception.AppException;
import com.bookstore.backend.repository.ImportRepository;
import com.bookstore.backend.repository.OrderRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
public class RevenueService {

    private final OrderRepository orderRepository;
    private final ImportRepository importRepository;
    private final RevenueExportService revenueExportService;

    public RevenueService(OrderRepository orderRepository, ImportRepository importRepository, RevenueExportService revenueExportService) {
        this.orderRepository = orderRepository;
        this.importRepository = importRepository;
        this.revenueExportService = revenueExportService;
    }

    @Transactional(readOnly = true)
    public byte[] exportRevenueToExcel(int year) {
        RevenueYearResponse data = getYearlyRevenue(year);
        return revenueExportService.exportRevenueToExcel(data);
    }


    @Transactional(readOnly = true)
    public RevenueSummaryResponse getMonthlyRevenue(int year, int month) {

        if (month < 1 || month > 12) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Month must be between 1 and 12");
        }

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = yearMonth.plusMonths(1).atDay(1).atStartOfDay();

        return buildSummary(year, month, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public RevenueYearResponse getYearlyRevenue(int year) {
        List<RevenueSummaryResponse> months = new ArrayList<>();
        BigDecimal revenue = BigDecimal.ZERO;
        BigDecimal importCost = BigDecimal.ZERO;
        long orderCount = 0L;
        long importCount = 0L;

        for (int month = 1; month <= 12; month++) {
            RevenueSummaryResponse summary = getMonthlyRevenue(year, month);
            months.add(summary);
            revenue = revenue.add(summary.revenue());
            importCost = importCost.add(summary.importCost());
            orderCount += summary.orderCount();
            importCount += summary.importCount();
        }

        LocalDateTime start = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(year + 1, 1, 1, 0, 0);
        List<TopProductResponse> topProducts = orderRepository.findTopProducts(start, end, PageRequest.of(0, 5));
        return new RevenueYearResponse(
                year,
                revenue,
                importCost,
                revenue.subtract(importCost),
                orderCount,
                importCount,
                months,
                topProducts
        );
    }

    @Transactional(readOnly = true)
    public Object getRevenue(int year, Integer month) {
        if (month == null) {
            return getYearlyRevenue(year);
        }
        return getMonthlyRevenue(year, month);
    }

    private RevenueSummaryResponse buildSummary(int year, Integer month, LocalDateTime startDate, LocalDateTime endDate) {
        BigDecimal revenue = defaultBigDecimal(orderRepository.sumCompletedRevenueByOrderDateBetween(startDate, endDate));
        Long orderCount = defaultLong(orderRepository.countCompletedOrdersByOrderDateBetween(startDate, endDate));
        BigDecimal importCost = defaultBigDecimal(importRepository.sumTotalCostByImportDateBetween(startDate, endDate));
        Long importCount = defaultLong(importRepository.countImportsByImportDateBetween(startDate, endDate));
        List<TopProductResponse> topProducts = orderRepository.findTopProducts(
                startDate, endDate, PageRequest.of(0, 5)); // Lấy top 5

        return new RevenueSummaryResponse(
                year,
                month,
                revenue,
                importCost,
                revenue.subtract(importCost),
                orderCount,
                importCount,
                topProducts // Gán vào DTO
        );
    }

    private BigDecimal defaultBigDecimal(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return value;
    }

    private BigDecimal defaultBigDecimal(Double value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(value);
    }

    private Long defaultLong(Long value) {
        if (value == null) {
            return 0L;
        }
        return value;
    }
}
