package com.example.tasteebackend.services;

import com.example.tasteebackend.dto.CreateOrderRequest;
import com.example.tasteebackend.dto.OrderResponse;
import com.example.tasteebackend.model.Order;
import com.example.tasteebackend.model.User;

import java.util.List;

public interface OrderService {
    Order createOrder(User customer, CreateOrderRequest request);

    List<OrderResponse> getCustomerOrders(User customer);

    List<OrderResponse> getAvailableOrders();

    OrderResponse acceptOrder(Long orderId, User deliveryDriver);

    List<OrderResponse> getRestaurantOrders(User restaurantOwner);

    List<OrderResponse> getOwnerOrderHistory(User restaurantOwner, String filter);

    OrderResponse getOrderById(Long orderId, User user);

    List<OrderResponse> getDeliveryDriverOrders(User deliveryDriver);

    OrderResponse finishOrder(Long orderId, User deliveryDriver);

    OrderResponse cancelDelivery(Long orderId, User deliveryDriver);

    List<OrderResponse> getDeliveryOrders(User deliveryDriver);
}