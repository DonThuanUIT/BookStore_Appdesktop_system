package com.bookstore.backend.dto.response;

import java.time.Instant;

public record JwtTokenResponse(
        String token,
        String tokenType,
        Instant issuedAt,
        Instant expiresAt
) {
}
