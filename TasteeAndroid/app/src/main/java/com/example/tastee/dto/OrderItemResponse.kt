package com.example.tastee.dto

data class OrderItemResponse(
    val dishId: Long,
    val dishName: String,
    val quantity: Int,
    val price: Float
)