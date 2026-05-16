package com.bookstore.backend.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ImportResponse(
        Long id,
        Long staffId,
        String staffUsername,
        Double totalCost,
        LocalDateTime importDate,
        List<ImportDetailResponse> details
) {
}
