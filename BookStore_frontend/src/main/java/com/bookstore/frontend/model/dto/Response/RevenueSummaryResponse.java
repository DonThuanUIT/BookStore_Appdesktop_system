package com.bookstore.frontend.model.dto.Response;

import java.math.BigDecimal;

public record RevenueSummaryResponse(
        Integer year,
        Integer month,
        BigDecimal revenue,
        BigDecimal importCost,
        BigDecimal profit,
        Long orderCount,
        Long importCount
) {}