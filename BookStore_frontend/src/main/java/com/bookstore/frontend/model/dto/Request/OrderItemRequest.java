package com.bookstore.frontend.model.dto.Request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OrderItemRequest(
        @JsonProperty("bookId")
        Long bookId,

        @JsonProperty("quantity")
        int quantity
) {}