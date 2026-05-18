package com.bookstore.backend.service;

import com.bookstore.backend.dto.request.UserCreateRequest;
import com.bookstore.backend.dto.request.UserUpdateRequest;
import com.bookstore.backend.dto.response.UserResponse;
import com.bookstore.backend.entity.User;

import java.util.List;

public interface UserService {
    User findByUsername(String username);

    List<UserResponse> getAll();

    UserResponse getById(Long id);

    UserResponse create(UserCreateRequest request);

    UserResponse update(Long id, UserUpdateRequest request);

    void delete(Long id);
}
