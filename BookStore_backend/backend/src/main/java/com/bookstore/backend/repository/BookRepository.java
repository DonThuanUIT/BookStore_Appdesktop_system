package com.bookstore.backend.repository;
import com.bookstore.backend.entity.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
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

    @Query(
            value = """
                    SELECT b
                    FROM Book b
                    WHERE b.isDeleted = false OR b.isDeleted IS NULL
                    """,
            countQuery = """
                    SELECT COUNT(b)
                    FROM Book b
                    WHERE b.isDeleted = false OR b.isDeleted IS NULL
                    """
    )
    Page<Book> findAllActive(Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT b
            FROM Book b
            WHERE b.id = :id AND (b.isDeleted = false OR b.isDeleted IS NULL)
            """)
    Optional<Book> findByIdActive(Long id);

    @EntityGraph(attributePaths = {"publisher", "authors", "categories"})
    @Query("""
            SELECT b
            FROM Book b
            WHERE b.id = :id AND (b.isDeleted = false OR b.isDeleted IS NULL)
            """)
    Optional<Book> findDetailsByIdActive(@Param("id") Long id);

    @EntityGraph(attributePaths = {"publisher", "authors", "categories"})
    @Query("""
            SELECT b
            FROM Book b
            WHERE (b.isDeleted = false OR b.isDeleted IS NULL)
              AND LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
            ORDER BY b.title ASC
            """)
    List<Book> findByTitleContainingIgnoreCaseActiveOrderByTitleAsc(@Param("keyword") String keyword);

    @EntityGraph(attributePaths = {"publisher", "authors", "categories"})
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
