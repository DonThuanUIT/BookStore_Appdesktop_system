package com.bookstore.backend.controller;

import com.bookstore.backend.dto.request.PaymentRequest;
import com.bookstore.backend.dto.response.PaymentResponse;
import com.bookstore.backend.entity.User;
import com.bookstore.backend.exception.AppException;
import com.bookstore.backend.service.PaymentService;
import com.bookstore.backend.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payment")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {
    private final PaymentService paymentService;
    private final UserService userService;

    public PaymentController(PaymentService paymentService, UserService userService) {
        this.paymentService = paymentService;
        this.userService = userService;
    }

    @PostMapping("/mock")
    public ResponseEntity<PaymentResponse> mockPayment(
            @Valid @RequestBody PaymentRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "You need to log in to pay for an order.");
        }

        User currentUser = userService.findByUsername(jwt.getSubject());
        return ResponseEntity.ok(paymentService.mockPayment(request.orderId(), currentUser));
    }
}
