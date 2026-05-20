package com.bookstore.backend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateOrderRequest(
        @NotEmpty(message = "The list of items cannot be left blank.")
        @Valid
        List<OrderItemRequest> items,

        @NotNull(message = "Payment method cannot be left blank.")
        String paymentMethod // <-- THÊM TRƯỜNG NÀY
) {}