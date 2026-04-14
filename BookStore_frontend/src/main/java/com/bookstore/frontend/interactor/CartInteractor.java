package com.bookstore.frontend.interactor;

import com.bookstore.frontend.model.BookDTO;
import com.bookstore.frontend.model.CartItemDTO;
import com.bookstore.frontend.model.CartModel;

public class CartInteractor {
    private final CartModel model;

    public CartInteractor(CartModel model) {
        this.model = model;
    }

    public void loadCartItems() {
        model.getItems().clear();
        BookDTO book = new BookDTO("2", "Manikkawatha", "Mahinda Prasad Masimbula", "Rs. 900/=", "book2.png");
        CartItemDTO item = new CartItemDTO(book, 1);

        item.quantityProperty().addListener((obs, oldVal, newVal) -> calculateTotal());

        model.getItems().add(item);
        calculateTotal();
    }

    public void calculateTotal() {
        double total = 0;
        for (CartItemDTO item : model.getItems()) {
            total += item.getSubtotal();
        }
        model.setTotalPrice(total);
    }
}
