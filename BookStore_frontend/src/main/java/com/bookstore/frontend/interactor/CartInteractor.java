package com.bookstore.frontend.interactor;

import com.bookstore.frontend.model.CartModel;
import com.bookstore.frontend.model.dto.CartItemDTO;
import com.bookstore.frontend.model.dto.Request.CreateOrderRequest;
import com.bookstore.frontend.model.dto.Request.OrderItemRequest;
import com.bookstore.frontend.service.api.ApiClient;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class CartInteractor {
    private final CartModel model;

    // Giữ nguyên Constructor nhận tham số model gốc của bạn
    public CartInteractor(CartModel model) {
        this.model = model;
    }

    public void loadCartItems() {
        model.refreshAggregates();
    }

    // Cập nhật hàm placeOrder: Nhận thêm paymentMethod và trả về CompletableFuture
    public CompletableFuture<Boolean> placeOrder(List<CartItemDTO> items, String paymentMethod) {
        try {
            List<OrderItemRequest> itemRequests = items.stream()
                    .map(item -> new OrderItemRequest(
                            item.getBook().getId(),
                            item.getQuantity()
                    ))
                    .collect(Collectors.toList());

            CreateOrderRequest request = new CreateOrderRequest(itemRequests, paymentMethod);

            // Gọi API bất đồng bộ tới endpoint chính xác của Backend
            return ApiClient.getInstance()
                    .post("/orders", request)
                    .thenApply(response -> {
                        if (response.statusCode() == 200 || response.statusCode() == 201) {
                            System.out.println("Đặt hàng thành công! Kết quả: " + response.body());
                            return true;
                        } else {
                            System.err.println("Backend từ chối đơn hàng! Mã lỗi: " + response.statusCode());
                            System.err.println("Chi tiết lỗi: " + response.body());
                            return false;
                        }
                    })
                    .exceptionally(ex -> {
                        System.err.println("Lỗi kết nối API: " + ex.getMessage());
                        return false;
                    });

        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(false);
        }
    }
}