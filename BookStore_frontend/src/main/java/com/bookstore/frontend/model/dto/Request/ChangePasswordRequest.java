package com.bookstore.frontend.model.dto.Request;

public record ChangePasswordRequest(
        String currentPassword,
        String newPassword
) {
}
