package com.bookstore.frontend.interactor;

import com.bookstore.frontend.util.BookMapper;
import com.bookstore.frontend.model.BookModel;
import com.bookstore.frontend.model.InventoryModel;
import com.bookstore.frontend.model.dto.Response.BookResponseDto;
import com.bookstore.frontend.model.dto.Request.BookUpsertRequestDto;
import com.bookstore.frontend.service.api.ApiClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class InventoryInteractor {
    private final InventoryModel model;

    public InventoryInteractor(InventoryModel model) {
        this.model = model;
    }

    public void loadInventoryData(int page, int size) {
        String endpoint = String.format("/books?page=%d&size=%d", page, size);

        ApiClient.getInstance().get(endpoint).thenAccept(response -> {
            if (response.statusCode() == 200) {
                try {
                    JsonNode root = ApiClient.getInstance().getMapper().readTree(response.body());
                    JsonNode content = root.get("content");

                    List<BookResponseDto> dtoList = ApiClient.getInstance().getMapper()
                            .readValue(content.traverse(), new TypeReference<List<BookResponseDto>>() {});

                    List<BookModel> bookList = new ArrayList<>();
                    int lowStock = 0;

                    // Sử dụng BookMapper chuẩn xác từ nhánh của bạn
                    for (BookResponseDto dto : dtoList) {
                        BookModel book = BookMapper.toModel(dto);

                        if (book.getQuantity() != null && book.getQuantity() < 10) {
                            lowStock++;
                        }
                        bookList.add(book);
                    }

                    final int finalLowStock = lowStock;

                    javafx.application.Platform.runLater(() -> {
                        model.getBooks().setAll(bookList);
                        model.totalTitlesProperty().set((int) root.get("totalElements").asLong());
                        model.lowStockCountProperty().set(finalLowStock);
                        model.paginationInfoProperty().set(String.format("Showing %d to %d of %d entries",
                                page * size + 1, (page * size) + bookList.size(), root.get("totalElements").asLong()));
                    });
                } catch (Exception e) {
                    System.err.println("Lỗi khi parse dữ liệu Inventory (DTO Mapper): " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    public CompletableFuture<Boolean> updateBook(BookModel book, File imageFile) {
        if (imageFile != null) {
            try {
                return ApiClient.getInstance().uploadFile("/images", imageFile).thenCompose(imgResponse -> {
                    if (imgResponse.statusCode() == 200) {
                        try {
                            JsonNode imgJson = ApiClient.getInstance().getMapper().readTree(imgResponse.body());
                            String cloudinaryUrl = imgJson.get("url").asText();
                            return sendUpdateRequest(book, cloudinaryUrl);
                        } catch (Exception e) {
                            System.err.println("ERROR parsing Cloudinary image JSON: " + e.getMessage());
                            return CompletableFuture.completedFuture(false);
                        }
                    }
                    return CompletableFuture.completedFuture(false);
                });
            } catch (Exception e) {
                System.err.println("ERROR reading local image file: " + e.getMessage());
                return CompletableFuture.completedFuture(false);
            }
        } else {
            return sendUpdateRequest(book, book.getImageUrl());
        }
    }

    private CompletableFuture<Boolean> sendUpdateRequest(BookModel book, String imageUrl) {
        try {
            if (imageUrl != null) {
                book.setImageUrl(imageUrl);
            }

            // Đồng bộ dữ liệu bằng Mapper thay vì Map<String, Object> thủ công
            BookUpsertRequestDto requestDTO = BookMapper.toUpsertRequest(book);
            String jsonBody = ApiClient.getInstance().getMapper().writeValueAsString(requestDTO);

            System.out.println("\n--- PAYLOAD GỬI ĐI TỪ INTERACTOR ---");
            System.out.println(jsonBody);

            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("http://localhost:8080/api/books/" + book.getId()))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + com.bookstore.frontend.util.UserSession.getInstance().getToken())
                    .PUT(java.net.http.HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            return java.net.http.HttpClient.newHttpClient()
                    .sendAsync(request, java.net.http.HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() == 200 || response.statusCode() == 201) {
                            System.out.println("=> CẬP NHẬT SÁCH THÀNH CÔNG!");
                            return true;
                        } else {
                            System.err.println("=> CẬP NHẬT SÁCH THẤT BẠI (HTTP " + response.statusCode() + "):");
                            System.err.println("Lý do từ Backend: " + response.body() + "\n");
                            return false;
                        }
                    });
        } catch (Exception e) {
            System.err.println("LỖI KHI GỬI REQUEST CẬP NHẬT: " + e.getMessage());
            e.printStackTrace();
            return CompletableFuture.completedFuture(false);
        }
    }

    public CompletableFuture<Boolean> deleteBook(Long bookId) {
        try {
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("http://localhost:8080/api/books/" + bookId))
                    .header("Authorization", "Bearer " + com.bookstore.frontend.util.UserSession.getInstance().getToken())
                    .DELETE()
                    .build();

            return java.net.http.HttpClient.newHttpClient()
                    .sendAsync(request, java.net.http.HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> response.statusCode() == 204 || response.statusCode() == 200);
        } catch (Exception e) {
            System.err.println("Lỗi khi xóa sách: " + e.getMessage());
            return CompletableFuture.completedFuture(false);
        }
    }
}