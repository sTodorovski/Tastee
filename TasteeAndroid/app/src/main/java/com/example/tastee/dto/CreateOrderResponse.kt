package com.example.tastee.dto

data class CreateOrderResponse(
    val orderId: Long,
    val total: Float,
    val clientSecret: String?
)