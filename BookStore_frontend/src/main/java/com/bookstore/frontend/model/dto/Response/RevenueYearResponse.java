package com.bookstore.frontend.model.dto.Response;

import java.math.BigDecimal;
import java.util.List;

public record RevenueYearResponse(
        Integer year,
        BigDecimal revenue,
        BigDecimal importCost,
        BigDecimal profit,
        Long orderCount,
        Long importCount,
        List<RevenueSummaryResponse> months,
        List<TopProductResponse> topProducts // THÊM DÒNG NÀY
) {}