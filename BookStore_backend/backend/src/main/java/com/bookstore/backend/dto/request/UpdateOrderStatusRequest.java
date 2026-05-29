package com.bookstore.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateOrderStatusRequest(
        @NotBlank(message = "Order status is required")
        @Pattern(
                regexp = "(?i)^(PENDING|SHIPPING|CANCELED|COMPLETED)$",
                message = "Order status must be PENDING, SHIPPING, CANCELED or COMPLETED"
        )
        @JsonProperty("status")
        String status
) {
}


