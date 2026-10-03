package com.example.tastee.dto

data class CreateOrderRequest(
    val restaurantId: Long,
    val items: List<CartItemRequest>,
    val deliveryAddress: String,
    val expressDelivery: Boolean,
    val tip: Float,
    val paymentMethod: String
)

data class CartItemRequest(
    val dishId: Long,
    val quantity: Int
)