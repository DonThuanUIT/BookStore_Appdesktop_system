package com.bookstore.frontend.model.dto.Response;

import java.time.LocalDateTime;

public class OrderResponseDTO {
    public Long id;
    public Double totalAmount;
    public Double discount; // <--- THÊM DÒNG NÀY
    public Double finalAmount;
    public String status;
    public String paymentMethod;
    public LocalDateTime orderDate;

    // Thêm các Getter tương ứng
    public Long getId() { return id; }
    public Double getTotalAmount() { return totalAmount; }
    public Double getDiscount() { return discount; } // <--- THÊM GETTER NÀY
    public Double getFinalAmount() { return finalAmount; }
    public String getStatus() { return status; }
    public String getPaymentMethod() { return paymentMethod; }
    public LocalDateTime getOrderDate() { return orderDate; }
}