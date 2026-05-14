package com.bookstore.backend.repository;
import com.bookstore.backend.entity.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface BookRepository extends JpaRepository<Book, Long>{

    @Query("""
            SELECT b
            FROM Book b
            WHERE b.isDeleted = false OR b.isDeleted IS NULL
            """)
    List<Book> findAllActive();

    @Query("""
            SELECT b
            FROM Book b
            WHERE b.id = :id AND (b.isDeleted = false OR b.isDeleted IS NULL)
            """)
    Optional<Book> findByIdActive(Long id);

    @Query("""
            SELECT DISTINCT b
            FROM Book b
            LEFT JOIN b.authors a
            LEFT JOIN b.categories c
            LEFT JOIN b.publisher p
            WHERE (b.isDeleted = false OR b.isDeleted IS NULL)
              AND (
                    LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR CAST(b.publishYear AS string) LIKE CONCAT('%', :keyword, '%')
              )
            """)
    List<Book> searchActive(String keyword);
}
