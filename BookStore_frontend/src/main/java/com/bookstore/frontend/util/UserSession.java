package com.bookstore.frontend.util;

import java.util.Collections;
import java.util.List;

public class UserSession {
    private static UserSession instance;
    private String token;
    private String username;
    private List<String> roles = List.of();

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    // Lưu Token vào đây sau khi login thành công
    public void init(String token, String username, List<String> roles) {
        this.token = token;
        this.username = username;
        this.roles = roles != null ? List.copyOf(roles) : List.of();
    }

    public String getToken() { return token; }
    public String getUsername() { return username; }

    public List<String> getRoles() {
        return Collections.unmodifiableList(roles);
    }

    /** true nếu JWT/login trả về role khách hàng (CUSTOMER hoặc ROLE_CUSTOMER). */
    public boolean isCustomer() {
        return roles.stream().anyMatch(UserSession::isCustomerRoleName);
    }

    private static boolean isCustomerRoleName(String role) {
        if (role == null) return false;
        String n = role.trim();
        return "CUSTOMER".equalsIgnoreCase(n) || "ROLE_CUSTOMER".equalsIgnoreCase(n);
    }

    public void clean() {
        token = null;
        username = null;
        roles = List.of();
    }
}