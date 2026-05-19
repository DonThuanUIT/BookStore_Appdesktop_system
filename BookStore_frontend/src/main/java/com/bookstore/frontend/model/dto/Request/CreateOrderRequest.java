package com.bookstore.frontend.model.dto.Request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record CreateOrderRequest(
        @JsonProperty("items")
        List<OrderItemRequest> items,

        @JsonProperty("paymentMethod")
        String paymentMethod
) {}