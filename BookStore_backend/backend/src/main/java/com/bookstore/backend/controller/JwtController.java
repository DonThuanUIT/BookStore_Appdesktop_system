package com.bookstore.backend.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bookstore.backend.dto.request.JwtGenerateRequest;
import com.bookstore.backend.dto.request.JwtValidationRequest;
import com.bookstore.backend.dto.response.JwtTokenResponse;
import com.bookstore.backend.dto.response.JwtValidationResponse;
import com.bookstore.backend.service.JwtService;

@RestController
@RequestMapping("/api/auth/jwt")
public class JwtController {

    private final JwtService jwtService;

    public JwtController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @PostMapping("/generate")
    public ResponseEntity<JwtTokenResponse> generateToken(@Valid @RequestBody JwtGenerateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(jwtService.generateToken(request.subject()));
    }

    @PostMapping("/validate")
    public ResponseEntity<JwtValidationResponse> validateToken(@Valid @RequestBody JwtValidationRequest request) {
        JwtValidationResponse response = jwtService.validateToken(request.token());
        HttpStatus status = response.valid() ? HttpStatus.OK : HttpStatus.UNAUTHORIZED;
        return ResponseEntity.status(status).body(response);
    }
}
