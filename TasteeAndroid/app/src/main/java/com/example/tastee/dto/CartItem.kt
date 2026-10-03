package com.example.tastee.dto

data class CartItem(
    val dishId: Long,
    val restaurantId: Long,
    val name: String,
    val price: Float,
    val image: String?,
    var quantity: Int = 1
)