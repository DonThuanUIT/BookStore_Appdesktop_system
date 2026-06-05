package com.bookstore.backend.controller;

import com.bookstore.backend.dto.request.CreateOrderRequest;
import com.bookstore.backend.dto.request.UpdateOrderStatusRequest;
import com.bookstore.backend.dto.response.OrderResponse;
import com.bookstore.backend.entity.User;
import com.bookstore.backend.exception.AppException;
import com.bookstore.backend.service.OrderService;
import com.bookstore.backend.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order", description = "Order management APIs")
@Validated
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrderResponse>> getAllOrders(
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "page must be greater than or equal to 0")
            int page,

            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "size must be at least 1")
            @Max(value = 100, message = "size must not exceed 100")
            int size,

            @RequestParam(defaultValue = "id")
            @Pattern(regexp = "^(id|status|orderDate|totalAmount|finalAmount)$", message = "sortBy is invalid")
            String sortBy,

            @RequestParam(defaultValue = "desc")
            @Pattern(regexp = "^(?i)(asc|desc)$", message = "direction must be asc or desc")
            String direction,

            @RequestParam(required = false) String status,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate endDate,
            @RequestParam(required = false) String search
    ) {
        java.time.LocalDateTime startDateTime = (startDate != null) ? startDate.atStartOfDay() : null;
        java.time.LocalDateTime endDateTime = (endDate != null) ? endDate.atTime(23, 59, 59, 999999999) : null;
        return ResponseEntity.ok(orderService.getAllOrders(page, size, sortBy, direction, status, startDateTime, endDateTime, search));
    }
    @GetMapping("/sales-history")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Page<OrderResponse>> getSalesHistory(
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "page must be greater than or equal to 0")
            int page,
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "size must be at least 1")
            @Max(value = 100, message = "size must not exceed 100")
            int size
    ) {
        return ResponseEntity.ok(orderService.getSalesHistory(page, size));
    }

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            Authentication authentication
    ) {
        String username = authentication.getName();
        User currentUser = userService.findByUsername(username);
        return ResponseEntity.ok(orderService.createOrder(request, currentUser));
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Page<OrderResponse>> getMyOrderHistory(
            @AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt,
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "page must be greater than or equal to 0")
            int page,

            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "size must be at least 1")
            @Max(value = 100, message = "size must not exceed 100")
            int size,

            @RequestParam(required = false) String status,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate endDate,
            @RequestParam(required = false) String search
    ) {
        if (jwt == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "You need to log in to view your history!");
        }

        String username = jwt.getSubject();
        User currentUser = userService.findByUsername(username);

        java.time.LocalDateTime startDateTime = (startDate != null) ? startDate.atStartOfDay() : null;
        java.time.LocalDateTime endDateTime = (endDate != null) ? endDate.atTime(23, 59, 59, 999999999) : null;

        return ResponseEntity.ok(orderService.getOrderHistory(currentUser, page, size, status, startDateTime, endDateTime, search));
    }

    @PostMapping("/{id}/confirm")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<OrderResponse> confirmReceived(
            @PathVariable @Positive(message = "id is invalid") Long id,
            @AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt
    ) {
        String username = jwt.getSubject();
        User currentUser = userService.findByUsername(username);

        return ResponseEntity.ok(orderService.confirmReceived(id, currentUser));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable @Positive Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(orderService.updateStatus(id, request.status()));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable @Positive Long id,
            @AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        User currentUser = userService.findByUsername(jwt.getSubject());
        return ResponseEntity.ok(orderService.cancelOrder(id, currentUser));
    }
}