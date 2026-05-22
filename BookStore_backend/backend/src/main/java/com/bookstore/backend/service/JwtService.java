package com.bookstore.backend.service;

import com.bookstore.backend.config.JwtProperties;
import com.bookstore.backend.dto.response.JwtTokenResponse;
import com.bookstore.backend.dto.response.JwtValidationResponse;
import com.bookstore.backend.util.RoleNames;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final JwtProperties jwtProperties;

    public JwtService(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder, JwtProperties jwtProperties) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.jwtProperties = jwtProperties;
    }

    public JwtTokenResponse generateToken(String subject, List<String> roles) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(jwtProperties.expirationMinutes(), ChronoUnit.MINUTES);
        List<String> normalizedRoles = RoleNames.normalizeAll(roles);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(subject)
                .issuer(jwtProperties.issuer())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim("roles", normalizedRoles)
                .build();

        String token = jwtEncoder.encode(
                JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)
        ).getTokenValue();

        return new JwtTokenResponse(token, "Bearer", subject, normalizedRoles, issuedAt, expiresAt);
    }

    public JwtValidationResponse validateToken(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            return new JwtValidationResponse(
                    true,
                    jwt.getSubject(),
                    RoleNames.normalizeAll(jwt.getClaimAsStringList("roles")),
                    jwt.getIssuedAt(),
                    jwt.getExpiresAt(),
                    "Token is valid"
            );
        } catch (JwtException | IllegalArgumentException ex) {
            return new JwtValidationResponse(false, null, null, null, null, ex.getMessage());
        }
    }
}
