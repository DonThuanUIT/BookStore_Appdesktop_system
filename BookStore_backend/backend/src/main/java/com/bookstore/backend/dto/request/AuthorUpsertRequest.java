package com.bookstore.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthorUpsertRequest(
        @NotBlank(message = "Tên tác giả không được để trống")
        @Size(max = 100, message = "Tên tác giả tối đa 100 ký tự")
        String name
) {
}

