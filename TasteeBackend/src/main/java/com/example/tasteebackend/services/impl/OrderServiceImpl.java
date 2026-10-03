package com.example.tasteebackend.services.impl;

import com.example.tasteebackend.dto.CreateOrderRequest;
import com.example.tasteebackend.dto.OrderItemRequest;
import com.example.tasteebackend.dto.OrderItemResponse;
import com.example.tasteebackend.dto.OrderResponse;
import com.example.tasteebackend.model.*;
import com.example.tasteebackend.model.enums.OrderStatus;
import com.example.tasteebackend.model.enums.PaymentMethod;
import com.example.tasteebackend.model.enums.Role;
import com.example.tasteebackend.repository.DishRepository;
import com.example.tasteebackend.repository.OrderRepository;
import com.example.tasteebackend.repository.RestaurantRepository;
import com.example.tasteebackend.services.OrderService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;
    private final DishRepository dishRepository;

    @Override
    @Transactional
    public Order createOrder(User customer, CreateOrderRequest request) {
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found"));

        if (!Boolean.TRUE.equals(restaurant.getOpen())) {
            throw new IllegalStateException("Restaurant is currently closed.");
        }

        Order order = new Order();
        order.setCustomer(customer);
        order.setRestaurant(restaurant);
        order.setPaymentMethod(request.getPaymentMethod());

        if (request.getPaymentMethod() == PaymentMethod.CARD) {
            order.setStatus(OrderStatus.PENDING_PAYMENT);
        } else if (request.getPaymentMethod() == PaymentMethod.CASH) {
            order.setStatus(OrderStatus.PAID);
        }

        order.setCreatedAt(LocalDateTime.now());
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setExpressDelivery(request.isExpressDelivery());
        order.setTip(request.getTip());

        float foodTotal = 0;

        for (OrderItemRequest itemRequest : request.getItems()) {
            Dish dish = dishRepository.findById(itemRequest.getDishId())
                    .orElseThrow(() -> new EntityNotFoundException("Dish not found"));

            if (!dish.getRestaurant().getId().equals(restaurant.getId())) {
                throw new IllegalArgumentException("Dish does not belong to restaurant.");
            }

            if (itemRequest.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than zero.");
            }

            float discount = dish.getDiscountPercentage() != null ? dish.getDiscountPercentage() : 0f;
            float discountedPrice = dish.getPrice() * (1f - discount / 100f);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setDish(dish);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPrice(discountedPrice);

            order.getItems().add(orderItem);
            foodTotal += discountedPrice * itemRequest.getQuantity();
        }

        float total = foodTotal;

        if (request.isExpressDelivery()) {
            total += 4.99f;
        }

        total += request.getTip();
        order.setTotal(total);

        return orderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getCustomerOrders(User customer) {
        return orderRepository.findByCustomerOrderByCreatedAtDesc(customer)
                .stream()
                .map(this::toOrderResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAvailableOrders() {
        return orderRepository.findByStatusAndDeliveryDriverIsNullOrderByCreatedAtAsc(OrderStatus.PAID)
                .stream()
                .map(this::toOrderResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getRestaurantOrders(User restaurantOwner) {
        Restaurant restaurant = restaurantRepository.findByOwner(restaurantOwner)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found for this user"));

        return orderRepository.findByRestaurantAndStatusOrderByCreatedAtAsc(restaurant, OrderStatus.PAID)
                .stream()
                .map(this::toOrderResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOwnerOrderHistory(User restaurantOwner, String filter) {
        Restaurant restaurant = restaurantRepository.findByOwner(restaurantOwner)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found for this user"));

        List<Order> orders = orderRepository.findByRestaurant(restaurant);

        if (filter != null && !filter.isBlank() && !filter.equalsIgnoreCase("ALL")) {
            LocalDateTime now = LocalDateTime.now();

            switch (filter.toUpperCase()) {
                case "TODAY":
                    LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
                    orders = orders.stream()
                            .filter(o -> o.getCreatedAt() != null && !o.getCreatedAt().isBefore(startOfDay))
                            .toList();
                    break;

                case "THIS_WEEK":
                    LocalDateTime startOfWeek = now.minusDays(7);
                    orders = orders.stream()
                            .filter(o -> o.getCreatedAt() != null && !o.getCreatedAt().isBefore(startOfWeek))
                            .toList();
                    break;

                case "THIS_MONTH":
                    LocalDateTime startOfMonth = now.minusDays(30);
                    orders = orders.stream()
                            .filter(o -> o.getCreatedAt() != null && !o.getCreatedAt().isBefore(startOfMonth))
                            .toList();
                    break;

                case "COMPLETED":
                    orders = orders.stream()
                            .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                            .toList();
                    break;

                case "CANCELLED":
                    orders = orders.stream()
                            .filter(o -> o.getStatus() == OrderStatus.CANCELLED)
                            .toList();
                    break;

                default:
                    break;
            }
        }

        return orders.stream()
                .sorted((o1, o2) -> {
                    if (o1.getCreatedAt() == null || o2.getCreatedAt() == null) {
                        return 0;
                    }
                    return o2.getCreatedAt().compareTo(o1.getCreatedAt());
                })
                .map(this::toOrderResponse)
                .toList();
    }

    @Override
    @Transactional
    public OrderResponse acceptOrder(Long orderId, User deliveryDriver) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if (order.getStatus() != OrderStatus.PAID) {
            throw new IllegalStateException("Order is no longer available.");
        }

        if (order.getDeliveryDriver() != null) {
            throw new IllegalStateException("Order has already been accepted.");
        }

        order.setDeliveryDriver(deliveryDriver);
        order.setStatus(OrderStatus.DELIVERING);
        orderRepository.save(order);

        return toOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId, User user) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if (user.getRole() == Role.ROLE_DELIVERY) {
            boolean availableOrder = order.getStatus() == OrderStatus.PAID && order.getDeliveryDriver() == null;
            boolean assignedToThisDriver = order.getDeliveryDriver() != null && order.getDeliveryDriver().getId().equals(user.getId());

            if (!availableOrder && !assignedToThisDriver) {
                throw new AccessDeniedException("You are not allowed to view this delivery.");
            }
        } else if (user.getRole() == Role.ROLE_RESTAURANT) {
            if (order.getRestaurant() == null || order.getRestaurant().getOwner() == null || !order.getRestaurant().getOwner().getId().equals(user.getId())) {
                throw new AccessDeniedException("You do not own this order's restaurant.");
            }
        } else {
            if (order.getCustomer() == null || !order.getCustomer().getId().equals(user.getId())) {
                throw new AccessDeniedException("You do not own this order.");
            }
        }

        return toOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getDeliveryDriverOrders(User deliveryDriver) {
        return orderRepository.findByDeliveryDriverOrderByCreatedAtDesc(deliveryDriver)
                .stream()
                .map(this::toOrderResponse)
                .toList();
    }

    @Override
    @Transactional
    public OrderResponse finishOrder(Long orderId, User deliveryDriver) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if (order.getDeliveryDriver() == null || !order.getDeliveryDriver().getId().equals(deliveryDriver.getId())) {
            throw new IllegalStateException("You are not assigned to this delivery.");
        }

        if (order.getStatus() != OrderStatus.DELIVERING) {
            throw new IllegalStateException("Order is not currently being delivered.");
        }

        order.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);

        return toOrderResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse cancelDelivery(Long orderId, User deliveryDriver) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if (order.getDeliveryDriver() == null || !order.getDeliveryDriver().getId().equals(deliveryDriver.getId())) {
            throw new IllegalStateException("You are not assigned to this delivery.");
        }

        if (order.getStatus() != OrderStatus.DELIVERING) {
            throw new IllegalStateException("Order is not currently being delivered.");
        }

        order.setDeliveryDriver(null);
        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        return toOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getDeliveryOrders(User deliveryDriver) {
        return orderRepository.findByDeliveryDriverOrderByCreatedAtDesc(deliveryDriver)
                .stream()
                .map(this::toOrderResponse)
                .toList();
    }

    private OrderResponse toOrderResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getRestaurant().getName(),
                order.getRestaurant().getLogo(),
                order.getTotal(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getDeliveryAddress(),
                order.isExpressDelivery(),
                order.getTip(),
                order.getItems()
                        .stream()
                        .map(orderItem -> new OrderItemResponse(
                                orderItem.getDish().getId(),
                                orderItem.getDish().getName(),
                                orderItem.getQuantity(),
                                orderItem.getPrice()
                        ))
                        .toList(),
                order.getPaymentMethod()
        );
    }
}