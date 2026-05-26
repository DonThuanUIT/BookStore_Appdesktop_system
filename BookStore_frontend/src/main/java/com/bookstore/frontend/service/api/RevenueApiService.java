package com.bookstore.frontend.service.api;

import com.bookstore.frontend.model.dto.Response.RevenueYearResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.http.HttpResponse;
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
        return apiClient.get("/revenue?year=" + year)
                .thenApply(res -> {
                    if (res.statusCode() == 200) {
                        try {
                            return mapper.readValue(res.body(), RevenueYearResponse.class);
                        } catch (Exception e) {
                            throw new RuntimeException("Lỗi chuyển đổi dữ liệu: " + e.getMessage());
                        }
                    }
                    throw new RuntimeException("Lỗi API: " + res.statusCode());
                });
    }
}