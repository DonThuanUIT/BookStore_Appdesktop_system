package com.bookstore.frontend.service.api;

import com.bookstore.frontend.model.dto.Request.CreateOrderRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class CartApiService {
    private static CartApiService instance;
    private CartApiService() {}
    public static CartApiService getInstance() {
        if (instance == null) instance = new CartApiService();
        return instance;
    }

    public CompletableFuture<HttpResponse<String>> placeOrder(CreateOrderRequest request) {
        return ApiClient.getInstance().post("/orders", request);
    }

    public CompletableFuture<HttpResponse<String>> validateVoucher(String code) {
        return ApiClient.getInstance().get("/api/vouchers/" + code);
    }
}