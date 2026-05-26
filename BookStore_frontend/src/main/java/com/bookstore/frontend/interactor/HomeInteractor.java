package com.bookstore.frontend.interactor;

import com.bookstore.frontend.model.BookModel;
import com.bookstore.frontend.model.HomeModel;
import com.bookstore.frontend.model.dto.Response.BookResponseDto;
import com.bookstore.frontend.service.api.ApiClient;
import com.bookstore.frontend.util.BookMapper; // KHAI THÁC SỨC MẠNH CỦA MAPPER
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
        String message = "Chào mừng, " + (username != null ? username : "Khách iu") + "!";
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

                            return dtoList.stream()
                                    .map(BookMapper::toModel)
                                    .collect(Collectors.toList());

                        } catch (Exception e) {
                            System.err.println("Lỗi Parse JSON tại HomeInteractor: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    return Collections.emptyList();
                });
    }
}