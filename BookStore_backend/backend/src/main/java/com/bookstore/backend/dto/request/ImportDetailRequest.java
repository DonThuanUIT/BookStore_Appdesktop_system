package com.bookstore.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ImportDetailRequest(
        @NotNull(message = "bookId is required")
        @Positive(message = "bookId is invalid")
        Long bookId,

        @NotNull(message = "Import quantity is required")
        @Positive(message = "Import quantity must be greater than 0")
        Integer quantity,

        @NotNull(message = "Import price is required")
        @Positive(message = "Import price must be greater than 0")
        Double importPrice
) {
}
