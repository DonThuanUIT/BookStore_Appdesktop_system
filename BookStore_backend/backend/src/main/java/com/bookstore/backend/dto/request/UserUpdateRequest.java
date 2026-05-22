package com.bookstore.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        @Pattern(regexp = "^[A-Za-z0-9_.-]+$", message = "Username contains invalid characters")
        String username,

        @Size(min = 6, max = 255, message = "Password must be between 6 and 255 characters")
        String password,

        @Email(message = "Email is invalid")
        @Size(max = 100, message = "Email must not exceed 100 characters")
        String email,

        @Size(max = 100, message = "Full name must not exceed 100 characters")
        String fullName,

        @Pattern(regexp = "^$|^[0-9+() .-]{7,15}$", message = "Phone number is invalid")
        String phone,

        @Size(max = 500, message = "Address must not exceed 500 characters")
        String address,

        @Pattern(regexp = "^(ROLE_)?(ADMIN|STAFF|CUSTOMER)$", flags = Pattern.Flag.CASE_INSENSITIVE, message = "Role is invalid")
        String roleName
) {
}
