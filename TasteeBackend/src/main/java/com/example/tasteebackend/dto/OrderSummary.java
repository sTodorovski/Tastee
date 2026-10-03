package com.example.tasteebackend.dto;

import java.time.LocalDateTime;

public class OrderSummary {
    private Long id;
    private String customerName;
    private Double totalAmount;
    private String status;
    private LocalDateTime createdAt;
}