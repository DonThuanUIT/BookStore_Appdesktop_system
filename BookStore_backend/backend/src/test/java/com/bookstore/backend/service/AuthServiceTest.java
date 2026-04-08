package com.bookstore.backend.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.bookstore.backend.dto.response.JwtTokenResponse;
import com.bookstore.backend.entity.AppUser;
import com.bookstore.backend.entity.Role;
import com.bookstore.backend.repository.AppUserRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldLoginAndReturnJwtWithRole() {
        AppUser user = new AppUser("admin", "encoded-password", Role.ADMIN);
        JwtTokenResponse tokenResponse = new JwtTokenResponse(
                "token-value",
                "Bearer",
                "admin",
                List.of("ADMIN"),
                Instant.now(),
                Instant.now().plusSeconds(3600)
        );

        when(appUserRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Admin@123", "encoded-password")).thenReturn(true);
        when(jwtService.generateToken("admin", List.of("ADMIN"))).thenReturn(tokenResponse);

        JwtTokenResponse response = authService.login("admin", "Admin@123");

        assertEquals("token-value", response.token());
        assertEquals(List.of("ADMIN"), response.roles());
    }

    @Test
    void shouldRejectInvalidPassword() {
        AppUser user = new AppUser("admin", "encoded-password", Role.ADMIN);

        when(appUserRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.login("admin", "wrong-password"));
    }
}
