package com.bookstore.backend.repository;

import org.springframework.stereotype.Repository;
import com.bookstore.backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findFirstByNameIgnoreCase(String name);
}
