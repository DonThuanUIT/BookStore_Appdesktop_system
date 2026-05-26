package com.bookstore.backend.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoleNamesTest {

    @Test
    void shouldNormalizeSupportedRoles() {
        assertEquals("ROLE_ADMIN", RoleNames.normalize("admin"));
        assertEquals("ROLE_CUSTOMER", RoleNames.normalize(" customer "));
    }

    @Test
    void shouldNormalizeAndDeduplicateRoleList() {
        assertEquals(List.of("ROLE_ADMIN"), RoleNames.normalizeAll(List.of("ADMIN", "ROLE_ADMIN")));
    }

    @Test
    void shouldRejectUnsupportedRole() {
        assertThrows(IllegalArgumentException.class, () -> RoleNames.normalize("STAFF"));
        assertThrows(IllegalArgumentException.class, () -> RoleNames.normalize("SUPER_ADMIN"));
    }

    @Test
    void shouldMatchAdminAndCustomer() {
        assertTrue(RoleNames.matchesAdmin("ADMIN"));
        assertTrue(RoleNames.matchesAdmin("ROLE_ADMIN"));
        assertTrue(RoleNames.matchesCustomer("CUSTOMER"));
        assertFalse(RoleNames.matchesAdmin("CUSTOMER"));
    }
}
