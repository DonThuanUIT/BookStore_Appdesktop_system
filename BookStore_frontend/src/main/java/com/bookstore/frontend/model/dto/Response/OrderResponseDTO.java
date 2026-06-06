package com.bookstore.frontend.model.dto.Response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;

import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderResponseDTO {
    public Long id;
    public Double totalAmount;
    public Double discount;
    public Double finalAmount;
    public String status;
    public String paymentMethod;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public LocalDateTime orderDate;

    public UserProfileResponseDto user;
    public List<OrderDetailResponseDTO> details;

    // Getters
    public Long getId() { return id; }
    public Double getTotalAmount() { return totalAmount; }
    public Double getDiscount() { return discount; }
    public Double getFinalAmount() { return finalAmount; }
    public String getStatus() { return status; }
    public String getPaymentMethod() { return paymentMethod; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public UserProfileResponseDto getUser() { return user; }
    public List<OrderDetailResponseDTO> getDetails() { return details; }
}