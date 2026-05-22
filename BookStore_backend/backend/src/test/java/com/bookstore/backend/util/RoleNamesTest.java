package com.bookstore.backend.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RoleNamesTest {

    @Test
    void shouldNormalizeSupportedRoles() {
        assertEquals("ROLE_ADMIN", RoleNames.normalize("admin"));
        assertEquals("ROLE_STAFF", RoleNames.normalize("ROLE_STAFF"));
        assertEquals("ROLE_CUSTOMER", RoleNames.normalize(" customer "));
    }

    @Test
    void shouldNormalizeAndDeduplicateRoleList() {
        assertEquals(List.of("ROLE_ADMIN"), RoleNames.normalizeAll(List.of("ADMIN", "ROLE_ADMIN")));
    }

    @Test
    void shouldRejectUnsupportedRole() {
        assertThrows(IllegalArgumentException.class, () -> RoleNames.normalize("SUPER_ADMIN"));
    }
}
