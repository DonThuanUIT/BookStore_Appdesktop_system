package com.bookstore.frontend.service.api;

import com.bookstore.frontend.model.dto.Response.RevenueYearResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RevenueApiService {
    private static RevenueApiService instance;
    private final ApiClient apiClient;
    private final ObjectMapper mapper;


    private RevenueApiService() {
        this.apiClient = ApiClient.getInstance();
        this.mapper = apiClient.getMapper();
    }

    public static synchronized RevenueApiService getInstance() {
        if (instance == null) instance = new RevenueApiService();
        return instance;
    }

    public CompletableFuture<RevenueYearResponse> getRevenueByYear(int year) {
        // Gọi API với query parameter năm
        return apiClient.get("/revenue?year=" + year)
                .thenApply(res -> {
                    if (res.statusCode() == 200) {
                        try {
                            // Jackson sẽ tự động map JSON vào Record nếu tên field khớp
                            return mapper.readValue(res.body(), RevenueYearResponse.class);
                        } catch (Exception e) {
                            System.err.println("JSON Parsing Error: " + e.getMessage());
                            throw new RuntimeException("Dữ liệu trả về không đúng định dạng", e);
                        }
                    } else {
                        throw new RuntimeException("Server trả về lỗi: " + res.statusCode());
                    }
                });
    }

    public CompletableFuture<List<RevenueYearResponse>> getRecent12Years(int year) {
        return apiClient.get("/revenue/history?year=" + year)
                .thenApply(res -> {
                    if (res.statusCode() == 200) {
                        try {
                            return mapper.readValue(res.body(), new com.fasterxml.jackson.core.type.TypeReference<List<RevenueYearResponse>>(){});
                        } catch (Exception e) {
                            throw new RuntimeException("Lỗi phân tích dữ liệu lịch sử: " + e.getMessage());
                        }
                    } else {
                        throw new RuntimeException("Không thể tải báo cáo 12 năm. Mã lỗi: " + res.statusCode());
                    }
                });
    }

    public CompletableFuture<byte[]> exportRevenueToExcel(int year) {
        return apiClient.getBytes("/revenue/export?year=" + year)
                .thenApply(res -> {
                    if (res.statusCode() == 200) {
                        return res.body();
                    }
                    // Giúp debug: backend trả về gì khi lỗi (thường là JSON/HTML lỗi security)
                    String errorBody;
                    try {
                        errorBody = new String(res.body(), java.nio.charset.StandardCharsets.UTF_8);
                    } catch (Exception e) {
                        errorBody = "<unable to decode error body>";
                    }
                    throw new RuntimeException("Server trả về lỗi: " + res.statusCode() + "; body=" + errorBody);
                });
    }

}
