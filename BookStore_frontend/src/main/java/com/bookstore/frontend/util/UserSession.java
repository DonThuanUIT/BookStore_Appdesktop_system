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
        if (roles == null) {
            this.roles = List.of();
            return;
        }

        // Backend no longer has STAFF role, only ADMIN and CUSTOMER
        this.roles = roles.stream()
                .map(r -> {
                    if (r == null) return null;
                    return r.trim();
                })
                .filter(r -> r != null && !r.isBlank())
                .toList();
    }


    public String getToken() { return token; }
    public String getUsername() { return username; }

    public List<String> getRoles() {
        return Collections.unmodifiableList(roles);
    }

    /** Admin / Vendor (người bán). */
    public boolean isAdmin() {
        return roles.stream().anyMatch(UserSession::isAdminRoleName);
    }

    /** Customer (người mua). */
    public boolean isCustomer() {
        return roles.stream().anyMatch(UserSession::isCustomerRoleName);
    }

    private static boolean isAdminRoleName(String role) {
        if (role == null) return false;
        String n = role.trim();
        return "ADMIN".equalsIgnoreCase(n)
                || "ROLE_ADMIN".equalsIgnoreCase(n);
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
