package com.bookstore.backend.service;

import com.bookstore.backend.dto.response.PaymentResponse;
import com.bookstore.backend.entity.Order;
import com.bookstore.backend.entity.User;
import com.bookstore.backend.exception.AppException;
import com.bookstore.backend.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PaymentService {
    private static final String PAYMENT_SUCCESS = "SUCCESS";
    private static final String ORDER_STATUS_PENDING = "PENDING";
    private static final String ORDER_STATUS_PAID = "PAID";

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    public PaymentService(OrderRepository orderRepository, OrderService orderService) {
        this.orderRepository = orderRepository;
        this.orderService = orderService;
    }

    @Transactional
    public PaymentResponse mockPayment(Long orderId, User currentUser) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "No order found with ID: " + orderId));

        validatePaymentPermission(order, currentUser);

        if (ORDER_STATUS_PENDING.equalsIgnoreCase(order.getStatus())) {
            order.setStatus(ORDER_STATUS_PAID);
            order = orderRepository.save(order);
        } else if (!ORDER_STATUS_PAID.equalsIgnoreCase(order.getStatus())) {
            throw new AppException(HttpStatus.BAD_REQUEST,
                    "Only PENDING orders can be paid. Current status: " + order.getStatus());
        }

        LocalDateTime paidAt = LocalDateTime.now();
        return PaymentResponse.builder()
                .paymentStatus(PAYMENT_SUCCESS)
                .transactionId("MOCK-" + order.getId() + "-" + paidAt.toString().replace(":", "").replace(".", ""))
                .paidAt(paidAt)
                .order(orderService.convertToResponse(order))
                .build();
    }

    private void validatePaymentPermission(Order order, User currentUser) {
        if (currentUser == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "You need to log in to pay for an order.");
        }

        boolean ownsOrder = order.getUser() != null && order.getUser().getId().equals(currentUser.getId());
        boolean staffOrAdmin = currentUser.getRole() != null
                && ("ADMIN".equalsIgnoreCase(currentUser.getRole().getName())
                || "STAFF".equalsIgnoreCase(currentUser.getRole().getName()));

        if (!ownsOrder && !staffOrAdmin) {
            throw new AppException(HttpStatus.FORBIDDEN, "You do not have permission to pay for this order.");
        }
    }
}
