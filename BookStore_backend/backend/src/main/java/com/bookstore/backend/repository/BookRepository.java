package com.bookstore.backend.repository;
import com.bookstore.backend.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface BookRepository extends JpaRepository<Book, Long>{
}
