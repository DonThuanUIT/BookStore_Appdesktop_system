package com.bookstore.backend.util;

import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class RoleNames {
    public static final String ADMIN = "ROLE_ADMIN";
    public static final String STAFF = "ROLE_STAFF";
    public static final String CUSTOMER = "ROLE_CUSTOMER";

    private static final Set<String> VALID_ROLES = Set.of(ADMIN, STAFF, CUSTOMER);

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
}
