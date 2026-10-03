package com.example.tastee.dto

data class RestaurantOrderStatusResponse(
    val manuallyOpen: Boolean,
    val acceptingOrders: Boolean,
    val dayOfWeek: String,
    val openTime: String?,
    val closeTime: String?
)