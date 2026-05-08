package com.bookstore.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ImportDetailRequest(
        @NotNull(message = "bookId không được để trống")
        @Positive(message = "bookId không hợp lệ")
        Long bookId,

        @NotNull(message = "Số lượng nhập không được để trống")
        @Positive(message = "Số lượng nhập phải lớn hơn 0")
        Integer quantity,

        @NotNull(message = "Giá nhập không được để trống")
        @Positive(message = "Giá nhập phải lớn hơn 0")
        Double importPrice
) {
}
