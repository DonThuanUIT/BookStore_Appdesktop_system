package com.bookstore.backend.dto.response;

import java.time.Instant;
import java.util.List;

public record JwtTokenResponse(
        String token,
        String tokenType,
        String subject,
        List<String> roles,
        Instant issuedAt,
        Instant expiresAt
) {
}
