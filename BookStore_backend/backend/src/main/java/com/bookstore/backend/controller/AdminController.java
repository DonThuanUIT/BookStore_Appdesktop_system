package com.bookstore.backend.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin Only")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminController {
    @GetMapping("/users")
    public String getAllUsers(){
        return "Danh sách người dùng hệ thống";
    }
}
