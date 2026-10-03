package com.example.tasteebackend.controller;

import com.example.tasteebackend.dto.CreateOrderRequest;
import com.example.tasteebackend.dto.CreateOrderResponse;
import com.example.tasteebackend.dto.OrderResponse;
import com.example.tasteebackend.model.Order;
import com.example.tasteebackend.model.User;
import com.example.tasteebackend.model.enums.PaymentMethod;
import com.example.tasteebackend.model.enums.Role;
import com.example.tasteebackend.repository.OrderRepository;
import com.example.tasteebackend.services.OrderService;
import com.example.tasteebackend.services.PaymentService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final PaymentService paymentService;
    private final OrderRepository orderRepository;

    @PostMapping
    public CreateOrderResponse createOrder(
            @AuthenticationPrincipal User user,
            @RequestBody CreateOrderRequest request
    ) throws StripeException {
        Order order = orderService.createOrder(user, request);

        if (request.getPaymentMethod() == PaymentMethod.CARD) {
            PaymentIntent paymentIntent = paymentService.createPaymentIntent(order.getTotal());
            order.setPaymentIntentId(paymentIntent.getId());
            orderRepository.save(order);

            return new CreateOrderResponse(
                    order.getId(),
                    order.getTotal(),
                    paymentIntent.getClientSecret()
            );
        }

        return new CreateOrderResponse(
                order.getId(),
                order.getTotal(),
                null
        );
    }

    @GetMapping("/my-orders")
    public List<OrderResponse> getMyOrders(@AuthenticationPrincipal User user) {
        return orderService.getCustomerOrders(user);
    }

    @GetMapping("/available")
    public List<OrderResponse> getAvailableOrders(@AuthenticationPrincipal User user) {
        if (user.getRole() != Role.ROLE_DELIVERY) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Only delivery drivers can view available orders."
            );
        }
        return orderService.getAvailableOrders();
    }

    @PostMapping("/{orderId}/accept")
    public OrderResponse acceptOrder(@PathVariable Long orderId, @AuthenticationPrincipal User user) {
        if (user.getRole() != Role.ROLE_DELIVERY) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Only delivery drivers can accept orders."
            );
        }
        return orderService.acceptOrder(orderId, user);
    }

    @GetMapping("/restaurant-orders")
    public List<OrderResponse> getRestaurantOrders(@AuthenticationPrincipal User user) {
        if (user.getRole() != Role.ROLE_RESTAURANT) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Only restaurant users can view restaurant orders."
            );
        }
        return orderService.getRestaurantOrders(user);
    }

    @GetMapping("/owner-history")
    public List<OrderResponse> getOwnerOrderHistory(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String filter
    ) {
        if (user.getRole() != Role.ROLE_RESTAURANT) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Only restaurant owners can view order history."
            );
        }
        return orderService.getOwnerOrderHistory(user, filter);
    }

    @GetMapping("/{orderId}")
    public OrderResponse getOrderById(@PathVariable Long orderId, @AuthenticationPrincipal User user) {
        return orderService.getOrderById(orderId, user);
    }

    @GetMapping("/delivery-orders")
    public List<OrderResponse> getDeliveryDriverOrders(@AuthenticationPrincipal User user) {
        if (user.getRole() != Role.ROLE_DELIVERY) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Only delivery drivers can view delivery orders."
            );
        }
        return orderService.getDeliveryDriverOrders(user);
    }

    @PostMapping("/{orderId}/finish")
    public OrderResponse finishOrder(@PathVariable Long orderId, @AuthenticationPrincipal User user) {
        if (user.getRole() != Role.ROLE_DELIVERY) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Only delivery drivers can finish orders."
            );
        }
        return orderService.finishOrder(orderId, user);
    }

    @PostMapping("/{orderId}/cancel-delivery")
    public OrderResponse cancelDelivery(@PathVariable Long orderId, @AuthenticationPrincipal User user) {
        if (user.getRole() != Role.ROLE_DELIVERY) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Only delivery drivers can cancel deliveries."
            );
        }
        return orderService.cancelDelivery(orderId, user);
    }
}