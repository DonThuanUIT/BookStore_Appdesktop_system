package com.bookstore.backend.repository;

import com.bookstore.backend.entity.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

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
    Page<Import> search(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
            SELECT SUM(i.totalCost)
            FROM Import i
            WHERE i.importDate >= :startDate
              AND i.importDate < :endDate
            """)
    Double sumTotalCostByImportDateBetween(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    @Query("""
            SELECT COUNT(i)
            FROM Import i
            WHERE i.importDate >= :startDate
              AND i.importDate < :endDate
            """)
    Long countImportsByImportDateBetween(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);
}
