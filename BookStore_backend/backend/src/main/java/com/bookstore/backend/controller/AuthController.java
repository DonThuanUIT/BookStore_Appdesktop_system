package com.bookstore.backend.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bookstore.backend.dto.request.ChangePasswordRequest;
import com.bookstore.backend.dto.request.LoginRequest;
import com.bookstore.backend.dto.request.RegistrationRequest;
import com.bookstore.backend.dto.request.UserProfileUpdateRequest;
import com.bookstore.backend.dto.response.JwtTokenResponse;
import com.bookstore.backend.dto.response.UserProfileResponse;
import com.bookstore.backend.exception.AppException;
import com.bookstore.backend.service.AuthService;
import org.springframework.http.HttpStatus;
import com.bookstore.backend.dto.request.RegistrationOtpRequest;
import com.bookstore.backend.dto.request.RegistrationVerifyRequest;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }


    @PostMapping("/register/request-otp")
    public ResponseEntity<String> requestRegistrationOtp(@Valid @RequestBody RegistrationOtpRequest request) {
        authService.requestRegistrationOtp(request);
        return ResponseEntity.ok("Mã OTP đã được gửi thành công qua Email của bạn.");
    }

    @PostMapping("/register/verify")
    public ResponseEntity<UserProfileResponse> verifyAndRegister(@Valid @RequestBody RegistrationVerifyRequest request) {
        UserProfileResponse registeredUser = authService.verifyAndRegister(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request.username(), request.password()));
    }

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> currentUser(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(authService.getCurrentUserProfile(requireUsername(jwt)));
    }

    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateCurrentUser(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UserProfileUpdateRequest request
    ) {
        return ResponseEntity.ok(authService.updateCurrentUserProfile(requireUsername(jwt), request));
    }

    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        authService.changeCurrentUserPassword(requireUsername(jwt), request.currentPassword(), request.newPassword());
        return ResponseEntity.noContent().build();
    }

    private String requireUsername(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null || jwt.getSubject().isBlank()) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "You need to log in first");
        }
        return jwt.getSubject();
    }
}