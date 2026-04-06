package com.bookstore.backend.dto.response;

import java.time.Instant;

public record JwtValidationResponse(
        boolean valid,
        String subject,
        Instant issuedAt,
        Instant expiresAt,
        String message
) {
}
