package com.bookstore.backend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record BookUpsertRequest(
        @NotBlank(message = "Book title is required")
        @Size(max = 200, message = "Book title must not exceed 200 characters")
        String title,

        @NotNull(message = "Publish year is required")
        @Min(value = 0, message = "Publish year is invalid")
        @Max(value = 2100, message = "Publish year is invalid")
        Integer publishYear,

        @PositiveOrZero(message = "Sell price is invalid")
        BigDecimal sellPrice,

        @Size(max = 500, message = "Image URL must not exceed 500 characters")
        String imageUrl,

        @NotNull(message = "publisherId is required")
        @Positive(message = "publisherId is invalid")
        Long publisherId,

        @NotEmpty(message = "authorIds must not be empty")
        List<@NotNull(message = "authorId is required") @Positive(message = "authorId is invalid") Long> authorIds,

        @NotEmpty(message = "categoryIds must not be empty")
        List<@NotNull(message = "categoryId is required") @Positive(message = "categoryId is invalid") Long> categoryIds
) {
}
