package com.bookstore.backend.dto.response;

import java.math.BigDecimal;

public record TopProductResponse(
        String bookTitle,
        Long soldQuantity,
        BigDecimal revenue
) {}