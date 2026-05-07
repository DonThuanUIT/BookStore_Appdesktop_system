package com.bookstore.backend.dto.response;

import lombok.Builder;

import java.util.List;
@Builder
public record UserProfileResponse(
        Long id,
        String username,
        String fullName,
        String email,
        List<String> roles

) {
}
