package com.bookstore.frontend.service.api;

import com.bookstore.frontend.model.dto.Request.ChangePasswordRequest;
import com.bookstore.frontend.model.dto.Request.UserProfileUpdateRequest;
import com.bookstore.frontend.model.dto.Response.UserProfileResponseDto;

import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class UserApiService {
    private final ApiClient apiClient;

    public UserApiService() {
        this.apiClient = ApiClient.getInstance();
    }

    public CompletableFuture<UserProfileResponseDto> getCurrentProfile() {
        return apiClient.get("/auth/me")
                .thenApply(this::readProfileResponse);
    }

    public CompletableFuture<UserProfileResponseDto> updateCurrentProfile(UserProfileUpdateRequest request) {
        return apiClient.put("/auth/me", request)
                .thenApply(this::readProfileResponse);
    }

    public CompletableFuture<Boolean> changePassword(ChangePasswordRequest request) {
        return apiClient.put("/auth/me/password", request)
                .thenApply(response -> response.statusCode() == 204);
    }

    private UserProfileResponseDto readProfileResponse(HttpResponse<String> response) {
        if (response.statusCode() != 200) {
            throw new RuntimeException("HTTP Error: " + response.statusCode());
        }
        try {
            return apiClient.getMapper().readValue(response.body(), UserProfileResponseDto.class);
        } catch (Exception e) {
            throw new RuntimeException("JSON Error", e);
        }
    }
}
