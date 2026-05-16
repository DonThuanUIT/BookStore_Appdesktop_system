package com.bookstore.backend.dto.request;

import jakarta.validation.Valid;import jakarta.validation.constraints.NotEmpty;import java.util.List;public record      CreateOrderRequest(
        @NotEmpty(message = "The list of items cannot be left blank.")
        @Valid
        List<OrderItemRequest> items
) {}