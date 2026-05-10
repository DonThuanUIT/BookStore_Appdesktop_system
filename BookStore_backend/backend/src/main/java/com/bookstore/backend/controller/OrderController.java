package com.bookstore.backend.controller;

import com.bookstore.backend.dto.request.CreateOrderRequest;
import com.bookstore.backend.dto.request.UpdateOrderStatusRequest;
import com.bookstore.backend.dto.response.OrderResponse;
import com.bookstore.backend.entity.Order;
import com.bookstore.backend.entity.User;
import com.bookstore.backend.service.OrderService;
import com.bookstore.backend.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order", description = "Các API quản lý đơn hàng")
public class OrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private UserService userService;
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<Page<OrderResponse>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        return ResponseEntity.ok(orderService.getAllOrders(page, size, sortBy, direction));
    }
    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(orderService.updateStatus(id, request.status()));
    }
    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<OrderResponse> createOrder(
            @Valid
            @RequestBody CreateOrderRequest request,
            Authentication authentication) {

        String username = authentication.getName();
        User currentUser = userService.findByUsername(username);

        // 2. Gọi service để tạo đơn hàng
        return ResponseEntity.ok(orderService.createOrder(request, currentUser));
    }
}
