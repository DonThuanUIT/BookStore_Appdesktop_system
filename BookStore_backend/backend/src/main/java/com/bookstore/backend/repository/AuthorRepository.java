package com.bookstore.backend.repository;
import com.bookstore.backend.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long>{
    List<Author> findByNameContainingIgnoreCaseOrderByNameAsc(String keyword);
    Optional<Author> findFirstByNameIgnoreCaseOrderByIdAsc(String name);
}
