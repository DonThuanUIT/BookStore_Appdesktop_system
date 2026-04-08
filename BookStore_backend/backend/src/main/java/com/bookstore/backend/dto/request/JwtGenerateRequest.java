package com.bookstore.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record JwtGenerateRequest(
        @NotBlank(message = "subject must not be blank")
        String subject
) {
}
