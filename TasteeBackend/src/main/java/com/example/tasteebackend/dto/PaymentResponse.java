package com.example.tasteebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentResponse {
    private Long orderId;
    private String clientSecret;
    private Float total;
}