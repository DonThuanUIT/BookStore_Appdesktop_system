package com.bookstore.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistrationRequest(
        @NotBlank(message = "User không được để trống")
        String username,
        @NotBlank(message = "Password không được để trống")
        @Size(min = 6, message = "Password phải có ít nhất 6 ký tự")
        String password
) {
}
