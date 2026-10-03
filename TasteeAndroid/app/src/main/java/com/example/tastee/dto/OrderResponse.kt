package com.example.tastee.dto

data class OrderResponse(
    val id: Long,
    val restaurantName: String,
    val restaurantImageUrl: String?,
    val total: Float,
    val status: String,
    val createdAt: String,
    val deliveryAddress: String,
    val expressDelivery: Boolean,
    val tip: Float,
    val items: List<OrderItemResponse>,
    val paymentMethod: String
)