package com.bookstore.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateOrderStatusRequest(
        @NotBlank(message = "Order status is required")
        @Pattern(regexp = "(?i)^(SHIPPING|CANCELED)$", message = "Order status must be SHIPPING or CANCELED")
        String status
) {
}
