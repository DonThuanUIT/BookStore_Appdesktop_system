package com.bookstore.frontend.model.dto.Request;

public record UserProfileUpdateRequest(
        String email,
        String fullName,
        String phone,
        String address
) {
}
