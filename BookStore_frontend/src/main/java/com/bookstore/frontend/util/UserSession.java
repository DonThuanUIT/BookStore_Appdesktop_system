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

    public boolean isAdmin() {
        return roles.stream().anyMatch(UserSession::isAdminRoleName);
    }

    public boolean isCustomer() {
        return roles.stream().anyMatch(UserSession::isCustomerRoleName);
    }

    private static boolean isAdminRoleName(String role) {
        if (role == null) return false;
        String n = role.trim();
        return "ADMIN".equalsIgnoreCase(n) || "ROLE_ADMIN".equalsIgnoreCase(n);
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