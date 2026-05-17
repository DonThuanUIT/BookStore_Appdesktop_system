package com.bookstore.backend.dto.request;

import jakarta.validation.constraints.NotNull;

public record PaymentRequest(
        @NotNull(message = "Order ID is required.")
        Long orderId
) {
}
