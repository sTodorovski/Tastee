package com.example.tasteebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateOrderResponse {
    private Long orderId;
    private Float total;
    private String clientSecret;
}