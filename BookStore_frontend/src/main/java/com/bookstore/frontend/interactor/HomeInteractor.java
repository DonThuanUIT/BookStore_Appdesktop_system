package com.bookstore.frontend.interactor;

import com.bookstore.frontend.model.BookModel;
import com.bookstore.frontend.model.HomeModel;
import com.bookstore.frontend.model.dto.Response.BookResponseDto;
import com.bookstore.frontend.service.api.ApiClient;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
        String endpoint = "/books?page=0&size=10&sortBy=id&direction=desc";

        return ApiClient.getInstance().get(endpoint)
                .thenApply(res -> {
                    if (res.statusCode() == 200) {
                        try {
                            JsonNode root = ApiClient.getInstance().getMapper().readTree(res.body());
                            JsonNode contentNode = root.get("content");

                            List<BookResponseDto> dtoList = ApiClient.getInstance().getMapper()
                                    .readerForListOf(BookResponseDto.class)
                                    .readValue(contentNode);

                            return dtoList.stream().map(dto -> {
                                BookModel bookModel = new BookModel();
                                bookModel.setId(dto.getId());
                                bookModel.setTitle(dto.getTitle());
                                bookModel.setPrice(dto.getSellPrice() != null ? dto.getSellPrice().doubleValue() : 0.0);
                                bookModel.setImageUrl(dto.getImageUrl());
                                bookModel.setPublisherName(dto.getPublisherName());

                                // 1. LẤY TÁC GIẢ PHẲNG: Gọi trực tiếp getAuthorNames() từ DTO mới
                                if (dto.getAuthorNames() != null && !dto.getAuthorNames().isEmpty()) {
                                    bookModel.setAuthorName(dto.getAuthorNames().get(0));
                                } else {
                                    bookModel.setAuthorName("Unknown Author");
                                }

                                // 2. LẤY THỂ LOẠI PHẲNG: Gán thẳng danh sách mảng chuỗi
                                if (dto.getCategoryNames() != null) {
                                    bookModel.setCategoryNames(dto.getCategoryNames());
                                } else {
                                    bookModel.setCategoryNames(Collections.emptyList());
                                }

                                return bookModel;
                            }).collect(Collectors.toList());

                        } catch (Exception e) {
                            System.err.println("Lỗi Parse JSON tại HomeInteractor: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    return Collections.emptyList();
                });
    }
}