package com.bookstore.backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderResponse {
    private Long id;
    private Double totalAmount;
    private Double discount;
    private Double finalAmount;
    private String status;
    private UserProfileResponse user; // Tận dụng DTO UserProfileResponse bạn đã có
}