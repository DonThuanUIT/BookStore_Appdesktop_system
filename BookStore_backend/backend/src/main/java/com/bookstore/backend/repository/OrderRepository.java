package com.bookstore.backend.repository;

import com.bookstore.backend.dto.response.TopProductResponse;
import com.bookstore.backend.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o WHERE o.user.id = :userId")
    Page<Order> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.status = :status")
    Page<Order> findByStatus(@Param("status") String status, Pageable pageable);

    // Tính tổng doanh thu đơn hàng hoàn thành trong khoảng thời gian
    @Query("""
            SELECT SUM(o.finalAmount)
            FROM Order o
            WHERE o.orderDate >= :startDate
              AND o.orderDate < :endDate
              AND o.status = 'COMPLETED'
            """)
    java.math.BigDecimal sumCompletedRevenueByOrderDateBetween(@Param("startDate") LocalDateTime startDate,
                                                               @Param("endDate") LocalDateTime endDate);

    // Đếm số đơn hàng hoàn thành trong khoảng thời gian
    @Query("""
            SELECT COUNT(o)
            FROM Order o
            WHERE o.orderDate >= :startDate
              AND o.orderDate < :endDate
              AND o.status = 'COMPLETED'
            """)
    Long countCompletedOrdersByOrderDateBetween(@Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

    // Lấy Top sản phẩm bán chạy nhất
    @Query("""
        SELECT new com.bookstore.backend.dto.response.TopProductResponse(
            b.title,
            SUM(od.quantity),
            SUM(od.price * od.quantity)
        )
        FROM OrderDetail od
        JOIN od.book b
        JOIN od.order o
        WHERE o.orderDate >= :startDate AND o.orderDate < :endDate
          AND o.status = 'COMPLETED'
        GROUP BY b.title
        ORDER BY SUM(od.quantity) DESC
        """)
    List<TopProductResponse> findTopProducts(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    // Lấy doanh thu phân bổ theo danh mục
    @Query("""
        SELECT c.name, SUM(od.price * od.quantity)
        FROM OrderDetail od
        JOIN od.book b
        JOIN b.categories c
        JOIN od.order o
        WHERE o.orderDate >= :startDate AND o.orderDate < :endDate
          AND o.status = 'COMPLETED'
        GROUP BY c.name
        """)
    List<Object[]> getRevenueByCategory(@Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);
}
