package com.bookstore.backend.repository;

import com.bookstore.backend.entity.ImportDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ImportDetailRepository extends JpaRepository<ImportDetail, Long> {

    @Query("SELECT COALESCE(SUM(d.quantity), 0) FROM ImportDetail d WHERE d.book.id = :bookId")
    Long sumQuantityByBookId(@Param("bookId") Long bookId);
}
