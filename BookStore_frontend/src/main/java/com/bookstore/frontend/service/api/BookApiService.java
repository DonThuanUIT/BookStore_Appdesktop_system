package com.bookstore.frontend.service.api;

import com.bookstore.frontend.model.dto.BookResponseDto;
import com.bookstore.frontend.model.dto.PageResponseDto;
import com.bookstore.frontend.util.UserSession; // Import Két sắt chứa Token của bạn
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class BookApiService {

    private static final String BASE_URL = "http://localhost:8080/api/books";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public BookApiService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Lấy danh sách sách có phân trang (Chạy ngầm không làm đơ UI)
     */
    public CompletableFuture<PageResponseDto<BookResponseDto>> fetchBooks(int page, int size) {
        String url = String.format("%s?page=%d&size=%d", BASE_URL, page, size);

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET();

        // NẾU API CẦN BẢO MẬT: Tự động thò tay vào UserSession lấy Token
        String token = UserSession.getInstance().getToken();
        if (token != null && !token.isEmpty()) {
            requestBuilder.header("Authorization", "Bearer " + token);
        }

        HttpRequest request = requestBuilder.build();

        // Gửi request BẤT ĐỒNG BỘ (Asynchronous)
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    // Kiểm tra HTTP Status
                    if (response.statusCode() != 200) {
                        throw new RuntimeException("Lỗi máy chủ: HTTP " + response.statusCode());
                    }

                    // Parse JSON sang PageResponseDto
                    try {
                        return objectMapper.readValue(
                                response.body(),
                                new TypeReference<PageResponseDto<BookResponseDto>>() {}
                        );
                    } catch (Exception e) {
                        throw new RuntimeException("Lỗi khi đọc dữ liệu JSON: " + e.getMessage(), e);
                    }
                });
    }
}