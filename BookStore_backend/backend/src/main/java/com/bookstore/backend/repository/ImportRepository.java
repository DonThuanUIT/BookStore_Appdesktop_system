package com.bookstore.backend.repository;

import com.bookstore.backend.entity.Import;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImportRepository extends JpaRepository<Import, Long> {

    @Query("""
            SELECT DISTINCT i
            FROM Import i
            LEFT JOIN i.staff s
            LEFT JOIN ImportDetail d ON d.importOrder = i
            LEFT JOIN d.book b
            WHERE LOWER(s.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR CAST(i.id AS string) LIKE CONCAT('%', :keyword, '%')
               OR CAST(i.totalCost AS string) LIKE CONCAT('%', :keyword, '%')
               OR CAST(d.quantity AS string) LIKE CONCAT('%', :keyword, '%')
               OR CAST(d.importPrice AS string) LIKE CONCAT('%', :keyword, '%')
            """)
    List<Import> search(String keyword);
}
