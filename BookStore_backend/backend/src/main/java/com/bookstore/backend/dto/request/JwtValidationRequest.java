package com.bookstore.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record JwtValidationRequest(
        @NotBlank(message = "token must not be blank")
        String token
) {
}
