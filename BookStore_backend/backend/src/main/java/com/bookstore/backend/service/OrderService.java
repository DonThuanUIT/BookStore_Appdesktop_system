package com.bookstore.backend.service;

import com.bookstore.backend.dto.request.CreateOrderRequest;
import com.bookstore.backend.dto.request.OrderItemRequest;
import com.bookstore.backend.dto.response.OrderResponse;
import com.bookstore.backend.dto.response.UserProfileResponse;
import com.bookstore.backend.entity.Book;
import com.bookstore.backend.entity.Order;
import com.bookstore.backend.entity.OrderDetail;
import com.bookstore.backend.entity.User;
import com.bookstore.backend.exception.AppException;
import com.bookstore.backend.repository.BookRepository;
import com.bookstore.backend.repository.OrderDetailRepository;
import com.bookstore.backend.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.stream.Collectors;


@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private OrderDetailRepository orderDetailRepository;

    public Page<OrderResponse> getAllOrders(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Order> orderPage = orderRepository.findAll(pageable);
        return orderPage.map(this::convertToResponse);
    }

    OrderResponse convertToResponse(Order order) {
        UserProfileResponse userDto = null;
        if (order.getUser() != null) {
            String roleName = (order.getUser().getRole() != null)
                    ? order.getUser().getRole().getName()
                    : "USER";

            userDto = UserProfileResponse.builder()
                    .id(order.getUser().getId())
                    .username(order.getUser().getUsername())
                    .fullName(order.getUser().getFullName())
                    .email(order.getUser().getEmail())
                    .roles(java.util.List.of(roleName)) // Đưa 1 role vào List
                    .build();
        }

        return OrderResponse.builder()
                .id(order.getId())
                .totalAmount(order.getTotalAmount() != null ? order.getTotalAmount().doubleValue() : 0.0)
                .discount(order.getDiscount() != null ? order.getDiscount().doubleValue() : 0.0)
                .finalAmount(order.getFinalAmount() != null ? order.getFinalAmount().doubleValue() : 0.0)
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod()) // <-- Trả ngược về Response
                .orderDate(order.getOrderDate())
                .user(userDto)
                .build();
    }
    public OrderResponse updateStatus(Long id, String newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "No order found."));
        String currentStatus = order.getStatus();

        if (!"PENDING".equalsIgnoreCase(currentStatus) && !"PAID".equalsIgnoreCase(currentStatus)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only PENDING or PAID orders can have their status updated.");
        }
        if (!"SHIPPING".equalsIgnoreCase(newStatus) && !"CANCELED".equalsIgnoreCase(newStatus)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "New status is invalid (Only accepting SHIPPING or CANCELED orders)");
        }
        order.setStatus(newStatus.toUpperCase());
        return convertToResponse(orderRepository.save(order));
    }
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, User user) {
        Order order = Order.builder()
                .user(user)
                .status("PENDING")
                .paymentMethod(request.paymentMethod()) // <-- Map trường này sang Entity
                .totalAmount(BigDecimal.ZERO)
                .discount(BigDecimal.ZERO)
                .finalAmount(BigDecimal.ZERO)
                .build();
        Order savedOrder = orderRepository.save(order);
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest item : request.items()) {
            Book book = bookRepository.findByIdActive(item.bookId())
                    .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy sách với ID: " + item.bookId()));

            if(book.getQuantity() < item.quantity()){
                throw new AppException(HttpStatus.BAD_REQUEST,
                        "Sách '" + book.getTitle() + "' không đủ số lượng. Kho còn: " + book.getQuantity());
            }
            book.setQuantity(book.getQuantity() - item.quantity());
            bookRepository.save(book);
            OrderDetail detail = OrderDetail.builder()
                    .order(savedOrder)
                    .book(book)
                    .quantity(item.quantity())
                    .price(book.getSellPrice())
                    .build();

            orderDetailRepository.save(detail);

            BigDecimal itemTotal = book.getSellPrice().multiply(BigDecimal.valueOf(item.quantity()));
            total = total.add(itemTotal);
        }

        savedOrder.setTotalAmount(total);
        savedOrder.setFinalAmount(total);

        return convertToResponse(orderRepository.save(savedOrder));
    }
    public Page<OrderResponse> getOrderHistory (User user, int page, int size){
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Order> orderPage = orderRepository.findByUserId(user.getId(), pageable);
        return orderPage.map(this::convertToResponse);
    }
    @Transactional
    public OrderResponse confirmReceived(Long orderId, User user) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,"No orders with ID found: " +orderId ));
        if (order.getUser() == null || !order.getUser().getId().equals(user.getId())) {
            throw new AppException(HttpStatus.FORBIDDEN, "You do not have the authority to confirm this order!");
        }
        if (!"SHIPPING".equalsIgnoreCase(order.getStatus())) {
            throw new AppException(HttpStatus.BAD_REQUEST,
                    "Only orders currently in transit (SHIPPING) can be confirmed as received. Current status:" + order.getStatus());
        }
        order.setStatus("COMPLETED");
        return convertToResponse(orderRepository.save(order));
    }
}
