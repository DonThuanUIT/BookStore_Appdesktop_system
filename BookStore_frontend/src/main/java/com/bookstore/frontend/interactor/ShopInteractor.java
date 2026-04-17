package com.bookstore.frontend.interactor; // Lưu ý: Của bạn có thể là com.bookstore.frontend.navigation

import com.bookstore.frontend.model.BookModel;
import com.bookstore.frontend.model.ShopModel;

public class ShopInteractor {
    private final ShopModel model;

    public ShopInteractor(ShopModel model) {
        this.model = model;
    }

    /**
     * Giả lập việc gọi API lấy danh sách tất cả sách.
     */
    public void loadAllBooks() {
        // 1. Xóa dữ liệu cũ trước khi nạp mới
        model.getBooks().clear();

        // 2. Tạo dữ liệu giả với kiểu dữ liệu chuẩn (Long cho ID, Double cho Price)
        model.getBooks().addAll(
                createMockBook(1L, "Thunmanhandiya", "Mahagamasekara", 700.0),
                createMockBook(2L, "Manikkawatha", "Mahinda Prasad Masimbula", 900.0),
                createMockBook(3L, "Manjula Wediwardena", "Manjula Wediwardena", 990.0),
                createMockBook(4L, "The Book Thief", "Markus Zusak", 1200.0),
                createMockBook(5L, "Thunmanhandiya", "Mahagamasekara", 700.0),
                createMockBook(6L, "Thunmanhandiya", "Mahagamasekara", 700.0)
        );
    }

    /**
     * Hàm phụ (Helper) giúp khởi tạo BookModel nhanh gọn.
     */
    private BookModel createMockBook(Long id, String title, String authorName, Double price) {
        BookModel book = new BookModel();
        book.setId(id);
        book.setTitle(title);
        book.setAuthorName(authorName);
        book.setPrice(price);
        return book;
    }
}