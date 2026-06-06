package com.bookstore.backend.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    private Long id;
    private Double totalAmount;
    private Double discount;
    private Double finalAmount;
    private String status;
    private String paymentMethod; // <-- THÊM TRƯỜNG NÀY
    private LocalDateTime orderDate;
    private UserProfileResponse user;
    private List<OrderDetailResponse> details;
}