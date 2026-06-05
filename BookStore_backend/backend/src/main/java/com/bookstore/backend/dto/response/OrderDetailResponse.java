package com.bookstore.backend.dto.response;

public record OrderDetailResponse(
        Long id,
        Long bookId,
        String bookTitle,
        Integer quantity,
        Double price,
        Double lineTotal
) {
}
