package com.bookstore.frontend.service.api;

import com.bookstore.frontend.model.dto.Response.AuthorResponseDto;
import com.bookstore.frontend.model.dto.Response.CategoryResponseDto;
import com.bookstore.frontend.model.dto.Response.PublisherResponseDto;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MasterDataApiService {

    public CompletableFuture<List<AuthorResponseDto>> getAllAuthors() {
        return fetchList("/authors", new TypeReference<>() {});
    }

    public CompletableFuture<AuthorResponseDto> createAuthor(String name) {
        return createItem("/authors", name, AuthorResponseDto.class);
    }

    public CompletableFuture<List<CategoryResponseDto>> getAllCategories() {
        return fetchList("/categories", new TypeReference<>() {});
    }

    public CompletableFuture<CategoryResponseDto> createCategory(String name) {
        return createItem("/categories", name, CategoryResponseDto.class);
    }


    public CompletableFuture<List<PublisherResponseDto>> getAllPublishers() {
        return fetchList("/publishers", new TypeReference<>() {});
    }

    public CompletableFuture<PublisherResponseDto> createPublisher(String name) {
        return createItem("/publishers", name, PublisherResponseDto.class);
    }


    private <T> CompletableFuture<List<T>> fetchList(String endpoint, TypeReference<List<T>> typeRef) {
        return ApiClient.getInstance().get(endpoint).thenApply(res -> {
            if (res.statusCode() == 200) {
                try {
                    return ApiClient.getInstance().getMapper().readValue(res.body(), typeRef);
                } catch (Exception e) {
                    System.err.println("Lỗi Parse JSON GET " + endpoint + ": " + e.getMessage());
                }
            }
            return Collections.emptyList();
        });
    }

    private <T> CompletableFuture<T> createItem(String endpoint, String name, Class<T> clazz) {
        return ApiClient.getInstance().post(endpoint, Map.of("name", name)).thenApply(res -> {
            if (res.statusCode() == 200 || res.statusCode() == 201) {
                try {
                    return ApiClient.getInstance().getMapper().readValue(res.body(), clazz);
                } catch (Exception e) {
                    System.err.println("Lỗi Parse JSON POST " + endpoint + ": " + e.getMessage());
                }
            }
            return null;
        });
    }
}