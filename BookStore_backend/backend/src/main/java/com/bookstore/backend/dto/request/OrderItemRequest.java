package com.bookstore.backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequest(
        @NotNull(message = "Book ID cannot be left blank.")
        Long bookId,
        @Min(value = 1, message = "The minimum order quantity is 1.")
        Integer quantity
) {}
