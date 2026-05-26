package com.bookstore.backend.util;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Hệ thống 2 vai trò: ADMIN (Vendor – người bán) và CUSTOMER (người mua).
 */
public final class RoleNames {
    public static final String ADMIN = "ROLE_ADMIN";
    public static final String CUSTOMER = "ROLE_CUSTOMER";

    private static final Set<String> VALID_ROLES = Set.of(ADMIN, CUSTOMER);

    private RoleNames() {
    }

    public static String normalize(String roleName) {
        if (roleName == null || roleName.trim().isEmpty()) {
            throw new IllegalArgumentException("Role is required");
        }

        String normalized = roleName.trim().toUpperCase(Locale.ROOT);
        if (!normalized.startsWith("ROLE_")) {
            normalized = "ROLE_" + normalized;
        }
        if (!VALID_ROLES.contains(normalized)) {
            throw new IllegalArgumentException("Role is invalid: " + roleName);
        }
        return normalized;
    }

    public static List<String> normalizeAll(List<String> roleNames) {
        if (roleNames == null) {
            return List.of();
        }
        return roleNames.stream()
                .map(RoleNames::normalize)
                .distinct()
                .toList();
    }

    public static boolean matchesAdmin(String roleName) {
        return matches(roleName, ADMIN);
    }

    public static boolean matchesCustomer(String roleName) {
        return matches(roleName, CUSTOMER);
    }

    private static boolean matches(String roleName, String expected) {
        if (roleName == null || roleName.isBlank()) {
            return false;
        }
        String normalized = roleName.trim().toUpperCase(Locale.ROOT);
        if (!normalized.startsWith("ROLE_")) {
            normalized = "ROLE_" + normalized;
        }
        return expected.equals(normalized);
    }
}
