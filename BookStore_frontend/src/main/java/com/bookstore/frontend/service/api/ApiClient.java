package com.bookstore.frontend.service.api;

import com.bookstore.frontend.util.UserSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class ApiClient {
    private static ApiClient instance;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private static final String BASE_URL = "http://localhost:8080/api";

    private ApiClient() {
        // Khởi tạo HttpClient dùng chung, set timeout 10s để tránh treo app
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public static ApiClient getInstance() {
        if (instance == null) {
            instance = new ApiClient();
        }
        return instance;
    }

    public ObjectMapper getMapper() {
        return objectMapper;
    }

    /**
     * Hàm dùng chung cho các request POST
     * @param endpoint Ví dụ: "/auth/login"
     * @param body Đối tượng Java (sẽ tự động convert sang JSON)
     */
    public CompletableFuture<HttpResponse<String>> post(String endpoint, Object body) {
        try {
            // Jackson tự động chuyển Map/Object thành JSON an toàn
            String jsonBody = objectMapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + endpoint))
                    .header("Content-Type", "application/json")
                    // TODO: Sau này làm phần giỏ hàng, ta sẽ thêm header Bearer Token ở đây
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    public CompletableFuture<HttpResponse<String>> get(String endpoint) {
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + endpoint))
                    .GET();

            // Móc token từ Két sắt và gắn vào Header "Authorization: Bearer <token>"
            String token = UserSession.getInstance().getToken();
            if (token != null && !token.isEmpty()) {
                requestBuilder.header("Authorization", "Bearer " + token);
            }

            return httpClient.sendAsync(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}