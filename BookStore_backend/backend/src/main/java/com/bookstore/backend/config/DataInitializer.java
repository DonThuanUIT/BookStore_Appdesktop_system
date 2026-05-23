package com.bookstore.backend.config;

import com.bookstore.backend.entity.AppUser;
import com.bookstore.backend.entity.Role;
import com.bookstore.backend.repository.AppUserRepository;
import com.bookstore.backend.repository.RoleRepository;
import com.bookstore.backend.util.RoleNames;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner seedUsers(
            RoleRepository roleRepository,
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            seedRole(roleRepository, RoleNames.ADMIN, "Administrator");
            seedRole(roleRepository, RoleNames.STAFF, "Staff");
            seedRole(roleRepository, RoleNames.CUSTOMER, "Customer");

//            seedUser(appUserRepository, passwordEncoder, "admin", "Admin@123", roleRepository.findByName(RoleNames.ADMIN).orElseThrow());
//            seedUser(appUserRepository, passwordEncoder, "staff", "Staff@123", roleRepository.findByName(RoleNames.STAFF).orElseThrow());
//            seedUser(appUserRepository, passwordEncoder, "customer", "Customer@123", roleRepository.findByName(RoleNames.CUSTOMER).orElseThrow());
        };
    }

    private void seedRole(RoleRepository roleRepository, String name, String description) {
        if (roleRepository.findByName(name).isEmpty()) {
            roleRepository.save(new Role(name, description));
        }
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
