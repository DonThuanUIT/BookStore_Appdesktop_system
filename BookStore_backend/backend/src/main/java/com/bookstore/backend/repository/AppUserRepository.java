package com.bookstore.backend.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bookstore.backend.entity.AppUser;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);

    boolean existsByUsername(String username);

    @Query("""
            SELECT u
            FROM AppUser u
            JOIN u.role r
            WHERE r.name IN ('ROLE_ADMIN', 'ROLE_STAFF')
              AND (
                    LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR CAST(u.id AS string) LIKE CONCAT('%', :keyword, '%')
              )
            """)
    List<AppUser> searchEmployees(String keyword);
}
