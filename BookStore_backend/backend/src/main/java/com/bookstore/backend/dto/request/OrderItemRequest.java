package com.bookstore.backend.dto.request;

public record OrderItemRequest(
        Long bookId,
        Integer quantity
) {}
