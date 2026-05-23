package com.bookstore.backend.service;

import com.bookstore.backend.dto.request.RegistrationRequest;
import com.bookstore.backend.dto.request.UserProfileUpdateRequest;
import com.bookstore.backend.dto.response.JwtTokenResponse;
import com.bookstore.backend.dto.response.UserProfileResponse;
import com.bookstore.backend.entity.AppUser;
import com.bookstore.backend.entity.Role;
import com.bookstore.backend.entity.User;
import com.bookstore.backend.exception.AppException;
import com.bookstore.backend.repository.AppUserRepository;
import com.bookstore.backend.repository.RoleRepository;
import com.bookstore.backend.repository.UserRepository;
import com.bookstore.backend.util.RoleNames;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            AppUserRepository appUserRepository,
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.appUserRepository = appUserRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public void register(RegistrationRequest request) {
        String username = request.username().trim();
        if (appUserRepository.existsByUsername(username)) {
            throw new AppException(HttpStatus.CONFLICT, "Username already exists!");
        }

        Role customerRole = roleRepository.findByName(RoleNames.CUSTOMER)
                .orElseThrow(() -> new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "role CUSTOMER not found!"));

        AppUser user = new AppUser(
                username,
                passwordEncoder.encode(request.password()),
                customerRole
        );
        appUserRepository.save(user);
    }

    public JwtTokenResponse login(String username, String password) {
        AppUser user = appUserRepository.findByUsername(username.trim())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        return jwtService.generateToken(user.getUsername(), List.of(user.getRole().getName()));
    }

    public UserProfileResponse getCurrentUserProfile(String username) {
        User user = findActiveUserByUsername(username);
        return toProfileResponse(user);
    }

    @Transactional
    public UserProfileResponse updateCurrentUserProfile(String username, UserProfileUpdateRequest request) {
        User user = findActiveUserByUsername(username);

        if (hasText(request.email())) {
            String email = request.email().trim();
            if (userRepository.existsByEmailAndIdNot(email, user.getId())) {
                throw new AppException(HttpStatus.CONFLICT, "Email already exists");
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

        return toProfileResponse(userRepository.save(user));
    }

    @Transactional
    public void changeCurrentUserPassword(String username, String currentPassword, String newPassword) {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "User not found: " + username));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BadCredentialsException("Invalid current password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        appUserRepository.save(user);
    }

    private User findActiveUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .filter(user -> user.getIsDeleted() == null || !user.getIsDeleted())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "User not found: " + username));
    }

    private UserProfileResponse toProfileResponse(User user) {
        Role role = user.getRole();
        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .roles(role != null ? List.of(RoleNames.normalize(role.getName())) : List.of())
                .build();
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
}
