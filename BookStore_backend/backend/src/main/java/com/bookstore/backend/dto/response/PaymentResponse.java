package com.bookstore.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponse {
    private String paymentStatus;
    private String transactionId;
    private LocalDateTime paidAt;
    private OrderResponse order;
}
