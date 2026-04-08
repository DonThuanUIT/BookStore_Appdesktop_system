package com.bookstore.backend.dto.response;

import java.util.List;

public record UserProfileResponse(
        String username,
        List<String> roles
) {
}
