package com.example.tastee.dto

data class RestaurantRequest(
    val name: String,
    val email: String,
    val phoneNumber: String,
    val location: String,
    val workingHours: String,
    val description: String,
    val logo: String?,
    val cover: String?
)