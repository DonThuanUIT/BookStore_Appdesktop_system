package com.bookstore.frontend.interactor;

import com.bookstore.frontend.model.BookModel;
import com.bookstore.frontend.model.HomeModel;
import com.bookstore.frontend.service.api.ApiClient;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class HomeInteractor {
    private final HomeModel model;

    public HomeInteractor(HomeModel model) {
        this.model = model;
    }

    public void loadDashboardData(String username) {
        String message = "Welcome back, " + (username != null ? username : "Customer") + "!";
        model.welcomeMessageProperty().set(message);
    }
    public CompletableFuture<List<BookModel>> getLatestBooks() {
        // Gọi API của Spring Boot (Lấy trang 0, 5 cuốn, xếp theo ID giảm dần)
        return ApiClient.getInstance().get("/books?page=0&size=5&sortBy=id&direction=desc")
                .thenApply(res -> {
                    if (res.statusCode() == 200) {
                        try {
                            // Đọc mảng JSON "content" từ đối tượng Page của Spring Boot
                            JsonNode root = ApiClient.getInstance().getMapper().readTree(res.body());
                            JsonNode contentNode = root.get("content");

                            // Convert mảng JSON sang List<BookModel>
                            return ApiClient.getInstance().getMapper()
                                    .readerForListOf(BookModel.class)
                                    .readValue(contentNode);
                        } catch (Exception e) {
                            System.err.println("Lỗi Parse JSON sách: " + e.getMessage());
                            e.printStackTrace();
                        }
                    } else {
                        System.err.println("Lỗi gọi API sách: HTTP " + res.statusCode());
                    }
                    return Collections.emptyList(); // Nếu lỗi, trả về list rỗng
                });
    }
}
