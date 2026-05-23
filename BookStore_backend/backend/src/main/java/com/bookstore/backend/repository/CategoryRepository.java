package com.bookstore.backend.repository;

import org.springframework.stereotype.Repository;
import com.bookstore.backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByNameContainingIgnoreCaseOrderByNameAsc(String keyword);
}
