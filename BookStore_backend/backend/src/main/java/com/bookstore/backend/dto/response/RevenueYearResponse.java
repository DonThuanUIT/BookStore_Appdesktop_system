package com.bookstore.backend.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record RevenueYearResponse(
        Integer year,
        BigDecimal revenue,
        BigDecimal importCost,
        BigDecimal profit,
        Long orderCount,
        Long importCount,
        List<RevenueSummaryResponse> months
) {
}
