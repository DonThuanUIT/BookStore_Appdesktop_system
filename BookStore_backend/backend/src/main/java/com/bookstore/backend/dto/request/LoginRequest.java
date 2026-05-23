package com.bookstore.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "username must not be blank")
        @Size(max = 50, message = "username must not exceed 50 characters")
        String username,

        @NotBlank(message = "password must not be blank")
        @Size(max = 255, message = "password must not exceed 255 characters")
        String password
) {
}
