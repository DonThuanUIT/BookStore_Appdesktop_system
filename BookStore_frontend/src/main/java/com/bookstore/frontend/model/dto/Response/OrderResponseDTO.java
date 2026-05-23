package com.bookstore.frontend.model.dto.Response;

import com.bookstore.frontend.model.dto.UserProfileDTO;

import java.time.LocalDateTime;


public class OrderResponseDTO {
    public Long id;
    public Double totalAmount;
    public Double discount;
    public Double finalAmount;
    public String status;
    public String paymentMethod;
    public LocalDateTime orderDate;
    public UserProfileDTO user;
}