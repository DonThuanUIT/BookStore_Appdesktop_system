package com.bookstore.frontend.interactor;

import com.bookstore.frontend.model.BookModel;
import com.bookstore.frontend.model.ShopModel;
import com.bookstore.frontend.model.dto.Response.BookResponseDto;
import com.bookstore.frontend.service.api.ApiClient;
import com.bookstore.frontend.util.BookMapper; // KHAI THÁC SỨC MẠNH CỦA MAPPER
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ShopInteractor {

    private final ShopModel model;

    public ShopInteractor(ShopModel model) {
        this.model = model;
    }

    public CompletableFuture<PageDto<BookModel>> getBooksPage(int page, int size) {
        String endpoint = String.format("/books?page=%d&size=%d&sortBy=id&direction=desc", page, size);

        return ApiClient.getInstance().get(endpoint)
                .thenApply(res -> {
                    if (res.statusCode() == 200) {
                        try {
                            JsonNode root = ApiClient.getInstance().getMapper().readTree(res.body());
                            JsonNode contentNode = root.get("content");
                            boolean isLast = root.get("last").asBoolean();

                            List<BookResponseDto> dtoList = ApiClient.getInstance().getMapper()
                                    .readerForListOf(BookResponseDto.class)
                                    .readValue(contentNode);

                            // Dùng BookMapper để map tự động
                            List<BookModel> books = dtoList.stream()
                                    .map(BookMapper::toModel)
                                    .collect(Collectors.toList());

                            return new PageDto<>(books, isLast);

                        } catch (Exception e) {
                            System.err.println("Lỗi Parse JSON sách Shop: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    return new PageDto<>(Collections.emptyList(), true);
                });
    }

    public List<BookModel> applyClientSideFilters(
            List<BookModel> originalBooks,
            String searchKeyword,
            List<String> selectedCategories,
            Double minPrice,
            Double maxPrice,
            String sortType
    ) {
        if (originalBooks == null || originalBooks.isEmpty()) return Collections.emptyList();

        return originalBooks.stream()
                .filter(b -> {
                    if (searchKeyword == null || searchKeyword.trim().isEmpty()) return true;
                    String kw = searchKeyword.toLowerCase();
                    boolean matchTitle = b.getTitle() != null && b.getTitle().toLowerCase().contains(kw);

                    // Dùng getFormattedAuthors() chuẩn xác
                    boolean matchAuthor = b.getFormattedAuthors().toLowerCase().contains(kw);

                    return matchTitle || matchAuthor;
                })
                .filter(b -> {
                    double price = b.getPrice() != null ? b.getPrice() : 0.0;
                    boolean overMin = (minPrice == null || price >= minPrice);
                    boolean underMax = (maxPrice == null || price <= maxPrice);
                    return overMin && underMax;
                })
                .filter(b -> {
                    if (selectedCategories == null || selectedCategories.isEmpty()) return true;
                    List<String> bookCategories = b.getCategoryNames();
                    if (bookCategories == null || bookCategories.isEmpty()) return false;
                    return bookCategories.stream().anyMatch(selectedCategories::contains);
                })
                .sorted((b1, b2) -> {
                    if (sortType == null) return 0;
                    double p1 = b1.getPrice() != null ? b1.getPrice() : 0.0;
                    double p2 = b2.getPrice() != null ? b2.getPrice() : 0.0;
                    return switch (sortType) {
                        case "Price: Low to High" -> Double.compare(p1, p2);
                        case "Price: High to Low" -> Double.compare(p2, p1);
                        default -> 0;
                    };
                })
                .collect(Collectors.toList());
    }

    public static class PageDto<T> {
        private final List<T> content;
        private final boolean isLast;

        public PageDto(List<T> content, boolean isLast) {
            this.content = content;
            this.isLast = isLast;
        }
        public List<T> getContent() { return content; }
        public boolean isLast() { return isLast; }
    }

    public CompletableFuture<List<BookModel>> searchBooksFromBackend(String keyword) {
        com.bookstore.frontend.service.api.BookApiService apiService = new com.bookstore.frontend.service.api.BookApiService();

        return apiService.searchBooks(keyword)
                .thenApply(dtoList -> {
                    if (dtoList == null || dtoList.isEmpty()) {
                        return Collections.emptyList();
                    }

                    // Tiếp tục dùng BookMapper thay vì viết lại hàm set
                    return dtoList.stream()
                            .map(BookMapper::toModel)
                            .collect(Collectors.toList());
                });
    }
}