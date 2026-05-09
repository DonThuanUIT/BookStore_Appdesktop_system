package com.bookstore.backend.dto.response;

public record ImportDetailResponse(
        Long id,
        Long bookId,
        String bookTitle,
        Integer quantity,
        Double importPrice,
        Double lineTotal
) {
}
