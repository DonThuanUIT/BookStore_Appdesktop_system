package com.bookstore.backend.service;

import com.bookstore.backend.entity.User;

public interface UserService {
    User findByUsername(String username);
}
