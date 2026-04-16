package com.bookstore.backend.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record BookResponse(
        Long id,
        String title,
        Integer publishYear,
        BigDecimal sellPrice,
        String imageUrl,
        Boolean isDeleted,
        Long publisherId,
        String publisherName,
        List<Long> categoryIds,
        List<String> categoryNames
) {
}

