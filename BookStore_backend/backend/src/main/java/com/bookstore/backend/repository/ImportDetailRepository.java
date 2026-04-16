package com.bookstore.backend.repository;

import com.bookstore.backend.entity.ImportDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImportDetailRepository extends JpaRepository<ImportDetail, Long> {
}
