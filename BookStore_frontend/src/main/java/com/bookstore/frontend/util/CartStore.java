package com.bookstore.frontend.util;

import com.bookstore.frontend.model.BookModel;
import com.bookstore.frontend.model.CartModel;
import com.bookstore.frontend.model.dto.CartItemDTO;

/**
 * Giỏ hàng dùng chung cho toàn app (Home, Shop, Cart, badge trên MainLayout).
 */
public final class CartStore {

    private static volatile CartStore instance;
    private final CartModel model = new CartModel();

    private CartStore() {}

    public static CartStore getInstance() {
        if (instance == null) {
            synchronized (CartStore.class) {
                if (instance == null) {
                    instance = new CartStore();
                }
            }
        }
        return instance;
    }

    public CartModel getModel() {
        return model;
    }

    public void addBook(BookModel book, int quantity) {
        model.addBook(book, quantity);
    }

    public void removeItem(CartItemDTO item) {
        model.removeItem(item);
    }
}
