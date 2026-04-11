package com.bookstore.backend.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@Tag(name = "Cart")
@PreAuthorize("hasRole('ROLE_CUSTOMER')")
public class CartController {

    @GetMapping
    public String getMyCart() {
        return "Đây là giỏ hàng của bạn (Customer).";
    }
}