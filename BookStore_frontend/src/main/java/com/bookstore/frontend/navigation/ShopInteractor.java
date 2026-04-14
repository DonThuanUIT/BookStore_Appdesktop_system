package com.bookstore.frontend.navigation;

import com.bookstore.frontend.model.BookDTO;
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
        // 1. Xóa dữ liệu cũ trước khi nạp mới (tránh bị nhân đôi khi chuyển trang nhiều lần)
        model.getBooks().clear();

        // 2. Tạo dữ liệu giả bám sát thiết kế UI
        model.getBooks().addAll(
                new BookDTO("1", "Thunmanhandiya", "Mahagamasekara", "Rs. 700/=", "book1.png"),
                new BookDTO("2", "Manikkawatha", "Mahinda Prasad Masimbula", "Rs. 900/=", "book2.png"),
                new BookDTO("3", "Manjula Wediwardena", "Manjula Wediwardena", "Rs. 990/=", "book3.png"),
                new BookDTO("4", "The Book Thief", "Markus Zusak", "Rs. 1200/=", "book4.png"),
                new BookDTO("5", "Thunmanhandiya", "Mahagamasekara", "Rs. 700/=", "book1.png"),
                new BookDTO("6", "Thunmanhandiya", "Mahagamasekara", "Rs. 700/=", "book1.png")
        );
    }
}
