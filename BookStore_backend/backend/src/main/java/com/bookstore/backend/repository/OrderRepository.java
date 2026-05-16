package com.bookstore.backend.repository;
import com.bookstore.backend.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>{
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId")
    Page<Order> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("""
            SELECT SUM(o.finalAmount)
            FROM Order o
            WHERE o.orderDate >= :startDate
              AND o.orderDate < :endDate
              AND UPPER(o.status) = 'COMPLETED'
            """)
    java.math.BigDecimal sumCompletedRevenueByOrderDateBetween(@Param("startDate") LocalDateTime startDate,
                                                               @Param("endDate") LocalDateTime endDate);

    @Query("""
            SELECT COUNT(o)
            FROM Order o
            WHERE o.orderDate >= :startDate
              AND o.orderDate < :endDate
              AND UPPER(o.status) = 'COMPLETED'
            """)
    Long countCompletedOrdersByOrderDateBetween(@Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);
}
