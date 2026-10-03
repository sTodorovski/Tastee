package com.example.tasteebackend.dto;

import com.example.tasteebackend.model.enums.PaymentMethod;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {
    private Long restaurantId;
    private List<OrderItemRequest> items;
    private String deliveryAddress;
    private boolean expressDelivery;
    private float tip;
    private PaymentMethod paymentMethod;
}