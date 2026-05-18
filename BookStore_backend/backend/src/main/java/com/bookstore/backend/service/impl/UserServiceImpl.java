package com.bookstore.backend.service.impl;

import com.bookstore.backend.dto.request.UserCreateRequest;
import com.bookstore.backend.dto.request.UserUpdateRequest;
import com.bookstore.backend.dto.response.UserResponse;
import com.bookstore.backend.entity.Role;
import com.bookstore.backend.entity.User;
import com.bookstore.backend.exception.AppException;
import com.bookstore.backend.repository.RoleRepository;
import com.bookstore.backend.repository.UserRepository;
import com.bookstore.backend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng: " + username));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAll() {
        return userRepository.findAllActive().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        User user = findActiveById(id);
        return toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse create(UserCreateRequest request) {
        String username = trimRequired(request.username(), "Username không được để trống");
        String email = trimRequired(request.email(), "Email không được để trống");

        if (userRepository.existsByUsername(username)) {
            throw new AppException(HttpStatus.CONFLICT, "Username đã tồn tại");
        }
        if (userRepository.existsByEmail(email)) {
            throw new AppException(HttpStatus.CONFLICT, "Email đã tồn tại");
        }

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(request.password()))
                .email(email)
                .fullName(trimToNull(request.fullName()))
                .phone(trimToNull(request.phone()))
                .address(trimToNull(request.address()))
                .role(resolveRole(request.roleName()))
                .isDeleted(false)
                .build();

        return toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse update(Long id, UserUpdateRequest request) {
        User user = findActiveById(id);

        if (hasText(request.username())) {
            String username = request.username().trim();
            if (userRepository.existsByUsernameAndIdNot(username, id)) {
                throw new AppException(HttpStatus.CONFLICT, "Username đã tồn tại");
            }
            user.setUsername(username);
        }

        if (hasText(request.password())) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        if (hasText(request.email())) {
            String email = request.email().trim();
            if (userRepository.existsByEmailAndIdNot(email, id)) {
                throw new AppException(HttpStatus.CONFLICT, "Email đã tồn tại");
            }
            user.setEmail(email);
        }

        if (request.fullName() != null) {
            user.setFullName(trimToNull(request.fullName()));
        }
        if (request.phone() != null) {
            user.setPhone(trimToNull(request.phone()));
        }
        if (request.address() != null) {
            user.setAddress(trimToNull(request.address()));
        }
        if (hasText(request.roleName())) {
            user.setRole(resolveRole(request.roleName()));
        }

        return toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User user = findActiveById(id);
        user.setIsDeleted(true);
        userRepository.save(user);
    }

    private User findActiveById(Long id) {
        return userRepository.findByIdActive(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"));
    }

    private Role resolveRole(String roleName) {
        String normalizedRoleName = normalizeRoleName(roleName);
        return roleRepository.findByName(normalizedRoleName)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy role: " + normalizedRoleName));
    }

    private String normalizeRoleName(String roleName) {
        String value = trimRequired(roleName, "Role không được để trống").toUpperCase();
        return value.startsWith("ROLE_") ? value : "ROLE_" + value;
    }

    private String trimRequired(String value, String message) {
        if (!hasText(value)) {
            throw new AppException(HttpStatus.BAD_REQUEST, message);
        }
        return value.trim();
    }

    private String trimToNull(String value) {
        if (!hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private UserResponse toResponse(User user) {
        Role role = user.getRole();
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getAddress(),
                role != null ? role.getId() : null,
                role != null ? role.getName() : null,
                user.getIsDeleted()
        );
    }
}
