package com.bookstore.backend.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
@Tag(name = "Books")
public class BookController {

    @GetMapping
    public String getAllBooks() {
        return "Danh sách sách: Mọi người (bao gồm CUSTOMER) đều xem được.";
    }

    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_STAFF')")
    public String addBook() {
        return "Thêm sách thành công! (Chỉ dành cho ADMIN/STAFF)";
    }
}