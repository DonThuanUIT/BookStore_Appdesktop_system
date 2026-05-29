package com.bookstore.backend.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record RevenueSummaryResponse(
        Integer year,
        Integer month,
        BigDecimal revenue,
        BigDecimal importCost,
        BigDecimal profit,
        Long orderCount,
        Long importCount,
        List<TopProductResponse>topProducts // MỚI: Thêm danh sách top sản phẩm
) {
}
