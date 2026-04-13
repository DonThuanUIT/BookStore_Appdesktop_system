package com.bookstore.backend.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.bookstore.backend.config.JwtProperties;
import com.bookstore.backend.dto.response.JwtTokenResponse;
import com.bookstore.backend.entity.AppUser;
import com.bookstore.backend.entity.Role;
import com.bookstore.backend.repository.AppUserRepository;
import com.bookstore.backend.repository.RoleRepository;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties(
                "bookstore-secret-key-for-jwt-signing-2026-safe",
                "bookstore-backend",
                60
        );
        SecretKey secretKey = new SecretKeySpec(jwtProperties.secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        JwtEncoder jwtEncoder = new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        jwtDecoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(jwtProperties.issuer()));

        JwtService jwtService = new JwtService(jwtEncoder, jwtDecoder, jwtProperties);
        authService = new AuthService(appUserRepository, roleRepository, passwordEncoder, jwtService);
    }

    @Test
    void shouldLoginAndReturnJwtWithRole() {
        Role adminRole = new Role();
        adminRole.setName("ADMIN");
        AppUser user = new AppUser("admin", "encoded-password", adminRole);

        when(appUserRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Admin@123", "encoded-password")).thenReturn(true);

        JwtTokenResponse response = authService.login("admin", "Admin@123");

        assertEquals("Bearer", response.tokenType());
        assertEquals("admin", response.subject());
        assertEquals(List.of("ADMIN"), response.roles());
        assertNotNull(response.token());
        assertTrue(response.expiresAt().isAfter(Instant.now()));
    }

    @Test
    void shouldRejectInvalidPassword() {
        Role adminRole = new Role();
        adminRole.setName("ADMIN");
        AppUser user = new AppUser("admin", "encoded-password", adminRole);

        when(appUserRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.login("admin", "wrong-password"));
    }
}
