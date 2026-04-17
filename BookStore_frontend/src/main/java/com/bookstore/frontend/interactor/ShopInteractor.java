package com.bookstore.frontend.interactor;

import com.bookstore.frontend.model.BookModel;
import com.bookstore.frontend.model.ShopModel;
import com.bookstore.frontend.model.dto.PageResponseDto;
import com.bookstore.frontend.service.api.BookApiService;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ShopInteractor {
    private final ShopModel model;
    private final BookApiService apiService;

    public ShopInteractor(ShopModel model) {
        this.model = model;
        // Khởi tạo Service chuyên lo việc gọi HTTP
        this.apiService = new BookApiService();
    }

    /**
     * Lấy trang dữ liệu sách, chuyển đổi từ DTO sang Model.
     * Trả về CompletableFuture để không làm đơ giao diện.
     */
    public CompletableFuture<PageResponseDto<BookModel>> getBooksPage(int page, int size) {
        return apiService.fetchBooks(page, size)
                .thenApply(pageDto -> {
                    // 1. Chuyển đổi danh sách DTO sang danh sách Model UI
                    List<BookModel> bookModels = pageDto.getContent().stream().map(dto -> {
                        BookModel book = new BookModel();
                        book.setId(dto.getId());
                        book.setTitle(dto.getTitle());

                        // Xử lý an toàn: Lấy tác giả đầu tiên nếu danh sách không rỗng
                        book.setAuthorName(dto.getAuthorNames() != null && !dto.getAuthorNames().isEmpty() ? dto.getAuthorNames().get(0) : "Unknown");

                        // Chuyển BigDecimal sang Double cho giao diện
                        book.setPrice(dto.getSellPrice() != null ? dto.getSellPrice().doubleValue() : 0.0);
                        book.setImageUrl(dto.getImageUrl());
                        book.setDescription(dto.getDescription());

                        return book;
                    }).collect(Collectors.toList());

                    // 2. Tạo một PageResponseDto mới bọc BookModel để trả về cho Controller
                    PageResponseDto<BookModel> resultPage = new PageResponseDto<>();
                    resultPage.setContent(bookModels);
                    resultPage.setLast(pageDto.isLast()); // Cờ báo hiệu trang cuối
                    resultPage.setTotalElements(pageDto.getTotalElements());

                    return resultPage;
                });
    }
}