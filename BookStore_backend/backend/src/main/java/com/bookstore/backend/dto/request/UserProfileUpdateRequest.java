package com.bookstore.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserProfileUpdateRequest(
        @Email(message = "Email is invalid")
        @Size(max = 100, message = "Email must not exceed 100 characters")
        String email,

        @Size(max = 100, message = "Full name must not exceed 100 characters")
        String fullName,

        @Pattern(regexp = "^$|^[0-9+() .-]{7,15}$", message = "Phone number is invalid")
        String phone,

        @Size(max = 500, message = "Address must not exceed 500 characters")
        String address
) {
}
