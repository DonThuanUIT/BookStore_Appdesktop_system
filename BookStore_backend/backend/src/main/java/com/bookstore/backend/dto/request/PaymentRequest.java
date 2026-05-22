package com.bookstore.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PaymentRequest(
        @NotNull(message = "Order ID is required")
        @Positive(message = "Order ID is invalid")
        Long orderId
) {
}
