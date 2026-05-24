package com.bookstore.frontend.interactor;

import com.bookstore.frontend.model.BookModel;
import com.bookstore.frontend.model.InventoryModel;
import com.bookstore.frontend.service.api.ApiClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class InventoryInteractor {
    private final InventoryModel model;

    public InventoryInteractor(InventoryModel model) {
        this.model = model;
    }

    // --- 1. LẤY DANH SÁCH SÁCH (READ) ---
    public void loadInventoryData(int page, int size) {
        String endpoint = String.format("/books?page=%d&size=%d", page, size);

        ApiClient.getInstance().get(endpoint).thenAccept(response -> {
            if (response.statusCode() == 200) {
                try {
                    JsonNode root = ApiClient.getInstance().getMapper().readTree(response.body());
                    JsonNode content = root.get("content");

                    List<BookModel> bookList = new ArrayList<>();
                    int lowStock = 0;

                    for (JsonNode node : content) {
                        BookModel book = new BookModel();
                        book.setId(node.get("id").asLong());
                        book.setTitle(node.get("title").asText());
                        // Vẫn giữ lệnh parse price và quantity để HIỂN THỊ trên bảng TableView
                        book.setPrice(node.get("sellPrice").asDouble());
                        book.setQuantity(node.get("quantity").asInt());
                        book.setImageUrl(node.has("imageUrl") && !node.get("imageUrl").isNull() ? node.get("imageUrl").asText() : null);
                        if (node.has("description") && !node.get("description").isNull()) {
                            book.setDescription(node.get("description").asText());
                        }

                        if (node.has("authorNames") && !node.get("authorNames").isEmpty()) {
                            book.setAuthorName(node.get("authorNames").get(0).asText());
                        }

                        if (book.getQuantity() < 10) lowStock++;
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
                    System.err.println("Lỗi khi parse dữ liệu Inventory: " + e.getMessage());
                }
            }
        });
    }

    // --- 2. CẬP NHẬT SÁCH (UPDATE) ---
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

    // Gửi Request Cập nhật JSON Sách
    private CompletableFuture<Boolean> sendUpdateRequest(BookModel book, String imageUrl) {
        try {
            ObjectNode requestData = ApiClient.getInstance().getMapper().createObjectNode();
            requestData.put("title", book.getTitle());

            if (imageUrl != null) requestData.put("imageUrl", imageUrl);
            if (book.getDescription() != null) requestData.put("description", book.getDescription());

            // TODO: Nâng cấp lấy ID động từ ComboBox ở bước tiếp theo
            requestData.put("publisherId", 4);
            requestData.putArray("authorIds").add(2);
            requestData.putArray("categoryIds").add(1);

            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("http://localhost:8080/api/books/" + book.getId()))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + com.bookstore.frontend.util.UserSession.getInstance().getToken())
                    .PUT(java.net.http.HttpRequest.BodyPublishers.ofString(requestData.toString()))
                    .build();

            return java.net.http.HttpClient.newHttpClient()
                    .sendAsync(request, java.net.http.HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> response.statusCode() == 200);
        } catch (Exception e) {
            System.err.println("LỖI KHI GỬI REQUEST CẬP NHẬT: " + e.getMessage());
            return CompletableFuture.completedFuture(false);
        }
    }


    // --- 3. XÓA SÁCH (DELETE) ---
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