package com.bookstore.backend.repository;

import com.bookstore.backend.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("""
            SELECT u
            FROM User u
            WHERE u.isDeleted = false OR u.isDeleted IS NULL
            ORDER BY u.id
            """)
    List<User> findAllActive();

    @Query("""
            SELECT u
            FROM User u
            WHERE u.id = :id
              AND (u.isDeleted = false OR u.isDeleted IS NULL)
            """)
    Optional<User> findByIdActive(Long id);

    boolean existsByUsernameAndIdNot(String username, Long id);

    boolean existsByEmailAndIdNot(String email, Long id);
}
