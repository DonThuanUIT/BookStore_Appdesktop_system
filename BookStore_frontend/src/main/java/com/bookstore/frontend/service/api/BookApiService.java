package com.bookstore.frontend.service.api;

import com.bookstore.frontend.model.BookModel;
import com.bookstore.frontend.model.dto.Response.BookResponseDto;
import com.bookstore.frontend.model.dto.Response.PageResponseDto;
import com.bookstore.frontend.util.UserSession;
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

    public CompletableFuture<PageResponseDto<BookResponseDto>> fetchBooks(int page, int size) {
        String url = String.format("%s?page=%d&size=%d&sortBy=id&direction=desc", BASE_URL, page, size);
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().uri(URI.create(url)).GET();
        attachToken(requestBuilder);

        return httpClient.sendAsync(requestBuilder.build(), HttpResponse.BodyHandlers.ofString())
                .thenApply(this::handleResponse);
    }

    public CompletableFuture<Boolean> createBook(BookModel newBook) {
        return sendWriteRequest(BASE_URL, "POST", newBook);
    }

    public CompletableFuture<Boolean> updateBook(Long id, BookModel book) {
        String url = BASE_URL + "/" + id;
        return sendWriteRequest(url, "PUT", book);
    }

    private CompletableFuture<Boolean> sendWriteRequest(String url, String method, Object body) {
        try {
            String jsonBody = objectMapper.writeValueAsString(body);

            // BẬT ĐÈN: In ra Payload để xem Front-End đang gửi cái gì
            System.out.println("\n--- THỰC THI API " + method + " ---");
            System.out.println("URL: " + url);
            System.out.println("Payload: " + jsonBody);

            HttpRequest.Builder rb = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .method(method, HttpRequest.BodyPublishers.ofString(jsonBody));
            attachToken(rb);

            return httpClient.sendAsync(rb.build(), HttpResponse.BodyHandlers.ofString())
                    .thenApply(res -> {
                        if (res.statusCode() == 200 || res.statusCode() == 201) {
                            System.out.println("=> THÀNH CÔNG!");
                            return true;
                        } else {
                            // BẬT ĐÈN: In ra lý do Backend từ chối
                            System.err.println("=> API THẤT BẠI (HTTP " + res.statusCode() + "):");
                            System.err.println("Lý do từ Backend: " + res.body() + "\n");
                            return false;
                        }
                    });
        } catch (Exception e) {
            System.err.println("Lỗi nội bộ khi gửi Request: " + e.getMessage());
            e.printStackTrace();
            return CompletableFuture.completedFuture(false);
        }
    }

    private void attachToken(HttpRequest.Builder rb) {
        String token = UserSession.getInstance().getToken();
        if (token != null && !token.isEmpty()) rb.header("Authorization", "Bearer " + token);
    }

    private PageResponseDto<BookResponseDto> handleResponse(HttpResponse<String> res) {
        if (res.statusCode() != 200) throw new RuntimeException("HTTP Error: " + res.statusCode());
        try {
            return objectMapper.readValue(res.body(), new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException("JSON Error", e);
        }
    }

    /**
     * HOÀN THIỆN: Gọi API tìm kiếm tổng lực và map chính xác cấu trúc mảng dẹt
     */
    public CompletableFuture<java.util.List<BookResponseDto>> searchBooks(String keyword) {
        String encodedKeyword = "";
        try {
            encodedKeyword = java.net.URLEncoder.encode(keyword != null ? keyword.trim() : "", java.nio.charset.StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            encodedKeyword = keyword != null ? keyword.trim() : "";
        }

        String url = String.format("%s/search?keyword=%s", BASE_URL, encodedKeyword);

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().uri(URI.create(url)).GET();
        attachToken(requestBuilder);

        return httpClient.sendAsync(requestBuilder.build(), HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            return objectMapper.readValue(response.body(), new TypeReference<java.util.List<BookResponseDto>>() {});
                        } catch (Exception e) {
                            System.err.println("Lỗi Parse JSON tại BookApiService: " + e.getMessage());
                        }
                    } else {
                        System.err.println("Lỗi gọi API Search: HTTP " + response.statusCode());
                    }
                    return java.util.Collections.emptyList();
                });
    }
}