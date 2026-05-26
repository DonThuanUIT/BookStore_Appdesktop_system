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

    private static final String LEGACY_STAFF_ROLE = "ROLE_STAFF";

    @Bean
    public CommandLineRunner seedUsers(
            RoleRepository roleRepository,
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            seedRole(roleRepository, RoleNames.ADMIN, "Vendor (Người bán)");
            seedRole(roleRepository, RoleNames.CUSTOMER, "Customer (Người mua)");

            migrateLegacyStaffUsers(roleRepository, appUserRepository);

//            seedUser(appUserRepository, passwordEncoder, "admin", "Admin@123", roleRepository.findByName(RoleNames.ADMIN).orElseThrow());
//            seedUser(appUserRepository, passwordEncoder, "customer", "Customer@123", roleRepository.findByName(RoleNames.CUSTOMER).orElseThrow());
        };
    }

    /** Chuyển tài khoản STAFF cũ sang ADMIN (vendor). */
    private void migrateLegacyStaffUsers(RoleRepository roleRepository, AppUserRepository appUserRepository) {
        roleRepository.findByName(LEGACY_STAFF_ROLE).ifPresent(staffRole -> {
            Role adminRole = roleRepository.findByName(RoleNames.ADMIN).orElseThrow();
            appUserRepository.findAll().stream()
                    .filter(user -> user.getRole() != null && LEGACY_STAFF_ROLE.equals(user.getRole().getName()))
                    .forEach(user -> {
                        user.setRole(adminRole);
                        appUserRepository.save(user);
                    });
        });
    }

    private void seedRole(RoleRepository roleRepository, String name, String description) {
        roleRepository.findByName(name).ifPresentOrElse(
                existing -> {
                    if (existing.getDescription() == null || !description.equals(existing.getDescription())) {
                        existing.setDescription(description);
                        roleRepository.save(existing);
                    }
                },
                () -> roleRepository.save(new Role(name, description))
        );
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
