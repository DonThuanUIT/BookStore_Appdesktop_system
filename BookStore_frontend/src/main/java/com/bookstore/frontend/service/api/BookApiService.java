package com.bookstore.frontend.service.api;

import com.bookstore.frontend.model.BookModel;
import com.bookstore.frontend.model.dto.BookResponseDto;
import com.bookstore.frontend.model.dto.PageResponseDto;
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
        String url = String.format("%s?page=%d&size=%d", BASE_URL, page, size);
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().uri(URI.create(url)).GET();
        attachToken(requestBuilder);

        return httpClient.sendAsync(requestBuilder.build(), HttpResponse.BodyHandlers.ofString())
                .thenApply(this::handleResponse);
    }

    public CompletableFuture<Boolean> createBook(BookModel newBook) {
        return sendWriteRequest(BASE_URL, "POST", newBook);
    }

    // --- HÀM MỚI: CẬP NHẬT SÁCH ĐÃ CÓ ---
    public CompletableFuture<Boolean> updateBook(Long id, BookModel book) {
        String url = BASE_URL + "/" + id;
        return sendWriteRequest(url, "PUT", book);
    }

    private CompletableFuture<Boolean> sendWriteRequest(String url, String method, Object body) {
        try {
            String jsonBody = objectMapper.writeValueAsString(body);
            HttpRequest.Builder rb = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .method(method, HttpRequest.BodyPublishers.ofString(jsonBody));
            attachToken(rb);

            return httpClient.sendAsync(rb.build(), HttpResponse.BodyHandlers.ofString())
                    .thenApply(res -> res.statusCode() == 200 || res.statusCode() == 201);
        } catch (Exception e) {
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
}