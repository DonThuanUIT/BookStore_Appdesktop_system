package com.bookstore.backend.dto.request;

public record CreateOrderRequest(java.util.List<OrderItemRequest> items) {}