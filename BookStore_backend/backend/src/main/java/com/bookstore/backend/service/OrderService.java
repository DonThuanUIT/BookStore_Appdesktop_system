package com.bookstore.backend.service;

import com.bookstore.backend.dto.request.CreateOrderRequest;
import com.bookstore.backend.dto.request.OrderItemRequest;
import com.bookstore.backend.dto.response.BookResponse;
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
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final BookRepository bookRepository;
    private final OrderDetailRepository orderDetailRepository;

    private final SseNotificationService sseNotificationService;
    private final BookService bookService;

    public OrderService(OrderRepository orderRepository,
                        BookRepository bookRepository,
                        OrderDetailRepository orderDetailRepository,
                        SseNotificationService sseNotificationService,
                        @Lazy BookService bookService) {
        this.orderRepository = orderRepository;
        this.bookRepository = bookRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.sseNotificationService = sseNotificationService;
        this.bookService = bookService;
    }

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
                    .roles(java.util.List.of(roleName))
                    .build();
        }

        return OrderResponse.builder()
                .id(order.getId())
                .totalAmount(order.getTotalAmount() != null ? order.getTotalAmount().doubleValue() : 0.0)
                .discount(order.getDiscount() != null ? order.getDiscount().doubleValue() : 0.0)
                .finalAmount(order.getFinalAmount() != null ? order.getFinalAmount().doubleValue() : 0.0)
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .orderDate(order.getOrderDate())
                .user(userDto)
                .build();
    }

    @Transactional
    public OrderResponse updateStatus(Long id, String newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng với ID: " + id));
        String currentStatus = order.getStatus();
        String targetStatus = newStatus.toUpperCase();

        if ("COMPLETED".equalsIgnoreCase(currentStatus) || "CANCELED".equalsIgnoreCase(currentStatus)) {
            throw new AppException(HttpStatus.BAD_REQUEST,
                    "Không thể cập nhật đơn hàng đã hoàn thành (COMPLETED) hoặc đã hủy (CANCELED).");
        }
        if (!"SHIPPING".equalsIgnoreCase(targetStatus) &&
                !"CANCELED".equalsIgnoreCase(targetStatus) &&
                !"COMPLETED".equalsIgnoreCase(targetStatus)) {
            throw new AppException(HttpStatus.BAD_REQUEST,
                    "Trạng thái mới không hợp lệ. Chỉ chấp nhận: SHIPPING, CANCELED hoặc COMPLETED.");
        }

        if ("SHIPPING".equalsIgnoreCase(currentStatus) && "CANCELED".equalsIgnoreCase(targetStatus)) {
            throw new AppException(HttpStatus.BAD_REQUEST,
                    "Đơn hàng đang trong quá trình giao (SHIPPING), không thể thực hiện hủy đơn.");
        }

        if ("CANCELED".equalsIgnoreCase(targetStatus)) {
            if (order.getOrderDetails() != null) {
                for (OrderDetail detail : order.getOrderDetails()) {
                    Book book = detail.getBook();
                    if (book != null) {
                        book.setQuantity(book.getQuantity() + detail.getQuantity());
                        bookRepository.save(book);

                        try {
                            BookResponse updatedBook = bookService.getById(book.getId());
                            sseNotificationService.sendNotification("UPDATE_BOOK", updatedBook);
                        } catch (Exception e) {
                            System.err.println("Lỗi gửi SSE khi hủy đơn: " + e.getMessage());
                        }
                    }
                }
            }
        }

        order.setStatus(targetStatus);
        return convertToResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, User user) {
        Order order = Order.builder()
                .user(user)
                .status("PENDING")
                .paymentMethod(request.paymentMethod())
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

            try {
                BookResponse updatedBook = bookService.getById(book.getId());
                sseNotificationService.sendNotification("UPDATE_BOOK", updatedBook);
            } catch (Exception e) {
                System.err.println("Lỗi gửi SSE khi tạo đơn: " + e.getMessage());
            }

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

    public Page<OrderResponse> getSalesHistory(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Order> completedOrders = orderRepository.findByStatus("COMPLETED", pageable);

        return completedOrders.map(this::convertToResponse);
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