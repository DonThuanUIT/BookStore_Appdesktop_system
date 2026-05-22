package com.bookstore.backend.service;

import com.bookstore.backend.dto.request.RegistrationRequest;
import com.bookstore.backend.dto.response.JwtTokenResponse;
import com.bookstore.backend.entity.AppUser;
import com.bookstore.backend.entity.Role;
import com.bookstore.backend.exception.AppException;
import com.bookstore.backend.repository.AppUserRepository;
import com.bookstore.backend.repository.RoleRepository;
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
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            AppUserRepository appUserRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.appUserRepository = appUserRepository;
        this.roleRepository = roleRepository;
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
}
