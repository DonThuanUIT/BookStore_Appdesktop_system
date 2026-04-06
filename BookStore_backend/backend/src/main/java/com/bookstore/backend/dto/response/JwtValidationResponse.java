package com.bookstore.backend.dto.response;

import java.time.Instant;
import java.util.List;

public record JwtValidationResponse(
        boolean valid,
        String subject,
        List<String> roles,
        Instant issuedAt,
        Instant expiresAt,
        String message
) {
}
