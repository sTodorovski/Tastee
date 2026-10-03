package com.example.tasteebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderItemResponse {
    private Long dishId;
    private String dishName;
    private int quantity;
    private Float price;
}