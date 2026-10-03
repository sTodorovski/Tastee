package com.example.tasteebackend.dto;

import java.time.LocalDateTime;
import java.util.List;

public class OrderDetail {
    private Long id;
    private String customerName;
    private String customerPhone;
    private String deliveryAddress;
    private String status;
    private String paymentMethod;
    private Double subtotal;
    private Double deliveryFee;
    private Double tip;
    private Double totalAmount;
    private LocalDateTime createdAt;
    private List<OrderItem> items;
}