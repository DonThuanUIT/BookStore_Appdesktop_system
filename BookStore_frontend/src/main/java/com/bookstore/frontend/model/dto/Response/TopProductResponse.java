package com.bookstore.frontend.model.dto.Response;
import java.math.BigDecimal;

public record TopProductResponse(String bookTitle, Long soldQuantity, BigDecimal revenue) {}