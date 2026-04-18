package com.bookstore.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PublisherUpsertRequest(
        @NotBlank(message = "Tên nhà xuất bản không được để trống")
        @Size(max = 100, message = "Tên nhà xuất bản tối đa 100 ký tự")
        String name
) {
}

