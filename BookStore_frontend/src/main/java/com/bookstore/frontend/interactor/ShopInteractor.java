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

                        // Lấy Tên Tác giả từ danh sách đối tượng AuthorDto
                        if (dto.getAuthors() != null && !dto.getAuthors().isEmpty()) {
                            book.setAuthorName(dto.getAuthors().get(0).getName());
                        } else {
                            book.setAuthorName("Unknown");
                        }

                        // Lấy Tên Nhà Xuất Bản từ đối tượng PublisherDto (nếu bạn cần hiển thị)
                        if (dto.getPublisher() != null) {
                            book.setPublisherName(dto.getPublisher().getName());
                        }

                        // Lấy mô tả (Hiện tại JSON của bạn không có trường này, tạm set cứng hoặc bỏ qua)
                        book.setDescription("Một cuốn sách rất hay và đáng đọc.");

                        // Chuyển BigDecimal sang Double
                        book.setPrice(dto.getSellPrice() != null ? dto.getSellPrice().doubleValue() : 0.0);
                        book.setImageUrl(dto.getImageUrl());

                        return book;
                    }).collect(Collectors.toList());

                    // 2. Bọc lại vào PageResponseDto
                    PageResponseDto<BookModel> resultPage = new PageResponseDto<>();
                    resultPage.setContent(bookModels);
                    resultPage.setLast(pageDto.isLast());
                    resultPage.setTotalElements(pageDto.getTotalElements());

                    return resultPage;
                });
    }
}