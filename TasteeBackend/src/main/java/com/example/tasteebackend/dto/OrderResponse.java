package com.example.tasteebackend.dto;

import com.example.tasteebackend.model.enums.OrderStatus;
import com.example.tasteebackend.model.enums.PaymentMethod;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponse {

    private Long id;
    private String restaurantName;
    private String restaurantImageUrl;
    private Float total;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private String deliveryAddress;
    private boolean expressDelivery;
    private float tip;
    private List<OrderItemResponse> items;
    private PaymentMethod paymentMethod;

    public OrderResponse(
            Long id,
            String restaurantName,
            String restaurantImageUrl,
            Float total,
            OrderStatus status,
            LocalDateTime createdAt,
            String deliveryAddress,
            boolean expressDelivery,
            float tip,
            List<OrderItemResponse> items,
            PaymentMethod paymentMethod
    ) {
        this.id = id;
        this.restaurantName = restaurantName;
        this.restaurantImageUrl = restaurantImageUrl;
        this.total = total;
        this.status = status;
        this.createdAt = createdAt;
        this.deliveryAddress = deliveryAddress;
        this.expressDelivery = expressDelivery;
        this.tip = tip;
        this.items = items;
        this.paymentMethod = paymentMethod;
    }
}