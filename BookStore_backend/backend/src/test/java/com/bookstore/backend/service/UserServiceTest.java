package com.bookstore.backend.service;

import com.bookstore.backend.dto.request.UserCreateRequest;
import com.bookstore.backend.dto.request.UserUpdateRequest;
import com.bookstore.backend.dto.response.UserResponse;
import com.bookstore.backend.entity.Role;
import com.bookstore.backend.entity.User;
import com.bookstore.backend.repository.RoleRepository;
import com.bookstore.backend.repository.UserRepository;
import com.bookstore.backend.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, roleRepository, passwordEncoder);
    }

    @Test
    void shouldCreateUserWithEncodedPasswordAndNormalizedRole() {
        Role role = role(2L, "ROLE_STAFF");
        UserCreateRequest request = new UserCreateRequest(
                " staff01 ",
                "Secret123",
                "staff01@example.com",
                "Staff 01",
                "0909000000",
                "Ho Chi Minh",
                "staff"
        );

        when(userRepository.existsByUsername("staff01")).thenReturn(false);
        when(userRepository.existsByEmail("staff01@example.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_STAFF")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("Secret123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(10L);
            return user;
        });

        UserResponse response = userService.create(request);

        assertEquals(10L, response.id());
        assertEquals("staff01", response.username());
        assertEquals("staff01@example.com", response.email());
        assertEquals("ROLE_STAFF", response.roleName());
        assertFalse(response.isDeleted());
    }

    @Test
    void shouldUpdateUserFields() {
        User existing = user(10L, "old_user", "old@example.com", role(1L, "ROLE_CUSTOMER"));
        Role adminRole = role(2L, "ROLE_ADMIN");
        UserUpdateRequest request = new UserUpdateRequest(
                "admin01",
                "NewSecret123",
                "admin01@example.com",
                "Admin 01",
                null,
                null,
                "ROLE_ADMIN"
        );

        when(userRepository.findByIdActive(10L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByUsernameAndIdNot("admin01", 10L)).thenReturn(false);
        when(userRepository.existsByEmailAndIdNot("admin01@example.com", 10L)).thenReturn(false);
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(adminRole));
        when(passwordEncoder.encode("NewSecret123")).thenReturn("new-encoded-password");
        when(userRepository.save(existing)).thenReturn(existing);

        UserResponse response = userService.update(10L, request);

        assertEquals("admin01", response.username());
        assertEquals("admin01@example.com", response.email());
        assertEquals("Admin 01", response.fullName());
        assertEquals("ROLE_ADMIN", response.roleName());
        assertEquals("new-encoded-password", existing.getPassword());
    }

    @Test
    void shouldSoftDeleteUser() {
        User existing = user(10L, "customer01", "customer01@example.com", role(1L, "ROLE_CUSTOMER"));
        existing.setIsDeleted(false);
        when(userRepository.findByIdActive(10L)).thenReturn(Optional.of(existing));

        userService.delete(10L);

        assertEquals(true, existing.getIsDeleted());
        verify(userRepository).save(existing);
    }

    private User user(Long id, String username, String email, Role role) {
        User user = User.builder()
                .username(username)
                .password("encoded-password")
                .email(email)
                .role(role)
                .isDeleted(false)
                .build();
        user.setId(id);
        return user;
    }

    private Role role(Long id, String name) {
        Role role = new Role();
        role.setId(id);
        role.setName(name);
        return role;
    }
}
