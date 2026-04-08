package com.bookstore.backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.bookstore.backend.entity.AppUser;
import com.bookstore.backend.entity.Role;
import com.bookstore.backend.repository.AppUserRepository;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner seedUsers(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            seedUser(appUserRepository, passwordEncoder, "admin", "Admin@123", Role.ADMIN);
            seedUser(appUserRepository, passwordEncoder, "staff", "Staff@123", Role.STAFF);
            seedUser(appUserRepository, passwordEncoder, "customer", "Customer@123", Role.CUSTOMER);
        };
    }

    private void seedUser(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            String username,
            String rawPassword,
            Role role
    ) {
        if (!appUserRepository.existsByUsername(username)) {
            appUserRepository.save(new AppUser(username, passwordEncoder.encode(rawPassword), role));
        }
    }
}
