package com.bookstore.frontend.interactor;

import com.bookstore.frontend.model.BookModel; // Import BookModel
import com.bookstore.frontend.model.dto.CartItemDTO;
import com.bookstore.frontend.model.CartModel;

public class CartInteractor {
    private final CartModel model;

    public CartInteractor(CartModel model) {
        this.model = model;
    }

    public void loadCartItems() {
        model.getItems().clear();

        // 1. Sử dụng BookModel thay vì BookDTO
        BookModel book = new BookModel();
        book.setId(2L); // ID bây giờ là kiểu Long
        book.setTitle("Manikkawatha");
        book.setAuthorName("Mahinda Prasad Masimbula");
        book.setPrice(900.0); // Truyền trực tiếp số Double, không còn String "Rs. 900/="

        CartItemDTO item = new CartItemDTO(book, 1);

        // Lắng nghe sự thay đổi số lượng để tính lại tổng tiền tự động
        item.quantityProperty().addListener((obs, oldVal, newVal) -> calculateTotal());

        model.getItems().add(item);
        calculateTotal();
    }

    public void calculateTotal() {
        double total = 0;
        for (CartItemDTO item : model.getItems()) {
            total += item.getSubtotal(); // subtotal bây giờ tính toán rất an toàn
        }
        model.setTotalPrice(total);
    }
}