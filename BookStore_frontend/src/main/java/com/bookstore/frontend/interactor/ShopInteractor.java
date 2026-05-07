package com.bookstore.frontend.interactor;

import com.bookstore.frontend.model.BookModel;
import com.bookstore.frontend.model.ShopModel;
import com.bookstore.frontend.service.api.ApiClient;
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

    /**
     * Tạm thời: Kéo một lượng lớn sách (VD: 50 cuốn) về Client
     * Khi Backend có API lọc, ta sẽ truyền thêm các tham số minPrice, maxPrice, categoryId vào đây.
     */
    public CompletableFuture<PageDto<BookModel>> getBooksPage(int page, int size) {
        // Gọi API của Spring Boot (Hiện tại Backend chưa có param lọc nên ta chỉ truyền page & size)
        String endpoint = String.format("/books?page=%d&size=%d&sortBy=id&direction=desc", page, size);

        return ApiClient.getInstance().get(endpoint)
                .thenApply(res -> {
                    if (res.statusCode() == 200) {
                        try {
                            // Đọc mảng JSON "content" từ đối tượng Page của Spring Boot
                            JsonNode root = ApiClient.getInstance().getMapper().readTree(res.body());
                            JsonNode contentNode = root.get("content");
                            boolean isLast = root.get("last").asBoolean();

                            // Convert mảng JSON sang List<BookModel>
                            List<BookModel> books = ApiClient.getInstance().getMapper()
                                    .readerForListOf(BookModel.class)
                                    .readValue(contentNode);

                            return new PageDto<>(books, isLast);

                        } catch (Exception e) {
                            System.err.println("Lỗi Parse JSON sách Shop: " + e.getMessage());
                            e.printStackTrace();
                        }
                    } else {
                        System.err.println("Lỗi gọi API Shop: HTTP " + res.statusCode());
                    }
                    return new PageDto<>(Collections.emptyList(), true);
                });
    }

    /**
     * LỌC TẠM TRÊN CLIENT:
     * Nhận vào list sách gốc, lọc theo các thông số trên giao diện và trả ra list đã lọc.
     */
    public List<BookModel> applyClientSideFilters(
            List<BookModel> originalBooks,
            String searchKeyword,
            String categoryKeyword,
            Double minPrice,
            Double maxPrice,
            String sortType
    ) {
        if (originalBooks == null || originalBooks.isEmpty()) return Collections.emptyList();

        return originalBooks.stream()
                // 1. Lọc theo từ khóa (Tìm trong Title hoặc Author)
                .filter(b -> {
                    if (searchKeyword == null || searchKeyword.trim().isEmpty()) return true;
                    String kw = searchKeyword.toLowerCase();
                    boolean matchTitle = b.getTitle() != null && b.getTitle().toLowerCase().contains(kw);
                    boolean matchAuthor = b.getAuthorName() != null && b.getAuthorName().toLowerCase().contains(kw);
                    return matchTitle || matchAuthor;
                })
                // 2. Lọc theo khoảng giá
                .filter(b -> {
                    double price = b.getPrice() != null ? b.getPrice() : 0.0;
                    boolean overMin = (minPrice == null || price >= minPrice);
                    boolean underMax = (maxPrice == null || price <= maxPrice);
                    return overMin && underMax;
                })
                // TODO: 3. Lọc theo Category.
                // Hiện tại BookModel cần bổ sung biến categoryNames (List<String>) để map với Backend thì mới lọc chuẩn được.

                // 4. Sắp xếp (Sorting)
                .sorted((b1, b2) -> {
                    if (sortType == null) return 0;
                    double p1 = b1.getPrice() != null ? b1.getPrice() : 0.0;
                    double p2 = b2.getPrice() != null ? b2.getPrice() : 0.0;

                    if (sortType.equals("Price: Low to High")) return Double.compare(p1, p2);
                    if (sortType.equals("Price: High to Low")) return Double.compare(p2, p1);
                    return 0; // Mặc định giữ nguyên (Newest)
                })
                .collect(Collectors.toList());
    }

    // Class DTO nội bộ để truyền cục data về cho Controller
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
}