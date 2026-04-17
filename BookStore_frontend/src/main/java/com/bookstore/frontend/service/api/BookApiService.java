package com.bookstore.frontend.service.api;

import com.bookstore.frontend.model.dto.BookResponseDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

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

    public List<BookResponseDto> getAllBooks() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
                    .GET()
                    // .header("Authorization", "Bearer " + token) // Mở ra khi ráp xác thực Login
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return objectMapper.readValue(response.body(), new TypeReference<List<BookResponseDto>>() {});
            } else {
                System.err.println("API Error: Lỗi HTTP " + response.statusCode());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("Network Error khi gọi API Book: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}