package com.bookstore.frontend.interactor;

import com.bookstore.frontend.model.CartModel;

public class CartInteractor {
    private final CartModel model;

    public CartInteractor(CartModel model) {
        this.model = model;
    }

    /** Đồng bộ tổng khi mở màn Cart (dữ liệu đến từ {@link com.bookstore.frontend.util.CartStore}). */
    public void loadCartItems() {
        model.refreshAggregates();
    }

    public void calculateTotal() {
        model.refreshAggregates();
    }

    // Trong CartInteractor.java
    public boolean saveOrder(String orderId, double totalAmount, String status, String proofPath) {
        try {
            // Trung thực hiện gọi Repository hoặc ApiClient để gửi dữ liệu lên Server/DB
            // Ví dụ: Lưu mã đơn hàng, số tiền, trạng thái 'PENDING' và đường dẫn ảnh minh chứng
            System.out.println("Lưu đơn hàng: " + orderId + " | Ảnh minh chứng: " + proofPath);

            // Code thực tế sẽ tương tự như:
            // return orderRepository.insert(new Order(orderId, totalAmount, status, proofPath));

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}