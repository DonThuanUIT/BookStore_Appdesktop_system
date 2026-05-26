package com.bookstore.backend.service;

import com.bookstore.backend.dto.response.PaymentResponse;
import com.bookstore.backend.entity.Order;
import com.bookstore.backend.entity.Role;
import com.bookstore.backend.entity.User;
import com.bookstore.backend.exception.AppException;
import com.bookstore.backend.repository.BookRepository;
import com.bookstore.backend.repository.OrderDetailRepository;
import com.bookstore.backend.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private BookRepository bookRepository;
    @Mock
    private OrderDetailRepository orderDetailRepository;
    @Mock
    private SseNotificationService sseNotificationService;
    @Mock
    private BookService bookService;

    private OrderService orderService;
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, bookRepository, orderDetailRepository, sseNotificationService, bookService);
        paymentService = new PaymentService(orderRepository, orderService);
    }

    @Test
    void shouldReturnSuccessAndMarkOrderAsPaid() {
        User customer = customer(1L);
        Order order = pendingOrder(10L, customer);
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        PaymentResponse response = paymentService.mockPayment(10L, customer);

        assertEquals("SUCCESS", response.getPaymentStatus());
        assertEquals("PAID", order.getStatus());
        assertEquals("PAID", response.getOrder().getStatus());
        assertNotNull(response.getTransactionId());
        assertNotNull(response.getPaidAt());
        verify(orderRepository).save(order);
    }

    @Test
    void shouldRejectPaymentFromAnotherCustomer() {
        User owner = customer(1L);
        User otherCustomer = customer(2L);
        Order order = pendingOrder(10L, owner);

        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        assertThrows(AppException.class, () -> paymentService.mockPayment(10L, otherCustomer));
        verify(orderRepository, never()).save(order);
    }

    private Order pendingOrder(Long orderId, User user) {
        Order order = Order.builder()
                .user(user)
                .status("PENDING")
                .totalAmount(BigDecimal.valueOf(120000))
                .discount(BigDecimal.ZERO)
                .finalAmount(BigDecimal.valueOf(120000))
                .build();
        order.setId(orderId);
        return order;
    }

    private User customer(Long userId) {
        Role role = new Role();
        role.setName("CUSTOMER");

        User user = User.builder()
                .username("customer" + userId)
                .role(role)
                .build();
        user.setId(userId);
        return user;
    }
}