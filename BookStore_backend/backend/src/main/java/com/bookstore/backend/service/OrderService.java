package com.bookstore.backend.service;

import com.bookstore.backend.dto.response.OrderResponse;
import com.bookstore.backend.dto.response.UserProfileResponse;
import com.bookstore.backend.entity.Order;
import com.bookstore.backend.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.stream.Collectors;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    public Page<OrderResponse> getAllOrders(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Order> orderPage = orderRepository.findAll(pageable);
        return orderPage.map(this::convertToResponse);
    }

    private OrderResponse convertToResponse(Order order) {
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
                // Ép kiểu BigDecimal sang Double để khớp với DTO
                .totalAmount(order.getTotalAmount() != null ? order.getTotalAmount().doubleValue() : 0.0)
                .discount(order.getDiscount() != null ? order.getDiscount().doubleValue() : 0.0)
                .finalAmount(order.getFinalAmount() != null ? order.getFinalAmount().doubleValue() : 0.0)
                .status(order.getStatus())
                .user(userDto)
                .build();
    }
}