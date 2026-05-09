package com.bookstore.backend.dto.response;

import java.util.List;

public record ImportResponse(
        Long id,
        Long staffId,
        String staffUsername,
        Double totalCost,
        List<ImportDetailResponse> details
) {
}
