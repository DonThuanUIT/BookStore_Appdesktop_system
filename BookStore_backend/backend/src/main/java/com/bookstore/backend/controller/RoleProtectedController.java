package com.bookstore.backend.controller;

import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class RoleProtectedController {

    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, String> adminDashboard() {
        return Map.of("message", "Admin access granted");
    }

    @GetMapping("/staff/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public Map<String, String> staffDashboard() {
        return Map.of("message", "Staff access granted");
    }

    @GetMapping("/customer/profile")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
    public Map<String, String> customerProfile() {
        return Map.of("message", "Customer access granted");
    }
}
