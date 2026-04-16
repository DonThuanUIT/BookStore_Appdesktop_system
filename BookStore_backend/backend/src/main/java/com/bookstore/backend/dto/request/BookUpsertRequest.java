package com.bookstore.backend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record BookUpsertRequest(
        @NotBlank(message = "Tên sách không được để trống")
        @Size(max = 200, message = "Tên sách tối đa 200 ký tự")
        String title,


        @Min(value = 0, message = "Năm xuất bản không hợp lệ")
        @Max(value = 2100, message = "Năm xuất bản không hợp lệ")
        Integer publishYear,

        @PositiveOrZero(message = "Giá bán không hợp lệ")
        BigDecimal sellPrice,

        @Size(max = 500, message = "URL ảnh tối đa 500 ký tự")
        String imageUrl,

        @Positive(message = "publisherId không hợp lệ")
        Long publisherId,

        // "Loại sách" (category) - client gửi list id danh mục
        List<Long> categoryIds
) {
}

