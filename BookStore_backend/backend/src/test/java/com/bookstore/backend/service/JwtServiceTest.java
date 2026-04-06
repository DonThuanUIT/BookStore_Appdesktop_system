package com.bookstore.backend.service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.bookstore.backend.config.JwtProperties;
import com.bookstore.backend.dto.response.JwtTokenResponse;
import com.bookstore.backend.dto.response.JwtValidationResponse;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties(
                "bookstore-secret-key-for-jwt-signing-2026-safe",
                60
        );
        SecretKey secretKey = new SecretKeySpec(jwtProperties.secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        JwtEncoder jwtEncoder = new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));
        JwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        jwtService = new JwtService(jwtEncoder, jwtDecoder, jwtProperties);
    }

    @Test
    void shouldGenerateAndValidateToken() {
        JwtTokenResponse tokenResponse = jwtService.generateToken("bookstore-user");

        assertNotNull(tokenResponse.token());
        assertEquals("Bearer", tokenResponse.tokenType());
        assertNotNull(tokenResponse.issuedAt());
        assertNotNull(tokenResponse.expiresAt());

        JwtValidationResponse validationResponse = jwtService.validateToken(tokenResponse.token());

        assertTrue(validationResponse.valid());
        assertEquals("bookstore-user", validationResponse.subject());
        assertEquals("Token is valid", validationResponse.message());
        assertNotNull(validationResponse.issuedAt());
        assertNotNull(validationResponse.expiresAt());
    }

    @Test
    void shouldRejectInvalidToken() {
        JwtValidationResponse validationResponse = jwtService.validateToken("invalid-token");

        assertFalse(validationResponse.valid());
        assertNull(validationResponse.subject());
        assertNotNull(validationResponse.message());
    }
}
