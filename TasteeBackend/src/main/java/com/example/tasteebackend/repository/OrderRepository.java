package com.example.tasteebackend.repository;

import com.example.tasteebackend.model.Order;
import com.example.tasteebackend.model.Restaurant;
import com.example.tasteebackend.model.User;
import com.example.tasteebackend.model.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByPaymentIntentId(String paymentIntentId);

    List<Order> findByCustomerOrderByCreatedAtDesc(User customer);

    List<Order> findByStatusOrderByCreatedAtAsc(OrderStatus status);

    Optional<Order> findByDeliveryDriverAndStatus(User deliveryDriver, OrderStatus status);

    List<Order> findByStatusAndDeliveryDriverIsNullOrderByCreatedAtAsc(OrderStatus status);

    List<Order> findByRestaurantAndStatusOrderByCreatedAtAsc(Restaurant restaurant, OrderStatus status);

    List<Order> findByDeliveryDriverOrderByCreatedAtDesc(User deliveryDriver);

    List<Order> findByRestaurant(Restaurant restaurant);
}