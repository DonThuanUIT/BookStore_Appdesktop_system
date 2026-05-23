package com.bookstore.backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemRequest(
        @NotNull(message = "Book ID is required")
        @Positive(message = "Book ID is invalid")
        Long bookId,

        @NotNull(message = "Order quantity is required")
        @Min(value = 1, message = "The minimum order quantity is 1")
        Integer quantity
) {
}
