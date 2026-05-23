package com.bookstore.frontend.model.dto.Response;

import java.util.List;

public record UserProfileResponseDto(
        Long id,
        String username,
        String fullName,
        String email,
        String phone,
        String address,
        List<String> roles
) {
}
