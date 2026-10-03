package com.example.tasteebackend.dto;

import lombok.Data;

@Data
public class OrderItemRequest {
    private Long dishId;
    private Integer quantity;
}