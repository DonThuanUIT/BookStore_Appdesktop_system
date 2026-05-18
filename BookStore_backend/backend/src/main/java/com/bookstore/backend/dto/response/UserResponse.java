package com.bookstore.backend.dto.response;

public record UserResponse(
        Long id,
        String username,
        String email,
        String fullName,
        String phone,
        String address,
        Long roleId,
        String roleName,
        Boolean isDeleted
) {
}
