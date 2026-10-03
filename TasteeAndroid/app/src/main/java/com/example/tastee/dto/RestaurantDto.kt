package com.example.tastee.dto

data class Restaurant(
    val id: Long,
    val name: String,
    val email: String?,
    val phoneNumber: String?,
    val location: String?,
    val workingHours: String?,
    val description: String?,
    val logo: String?,
    val cover: String?,
    val open: Boolean?,
    val dishes: List<Dish>?,
    val workingHoursList: List<RestaurantWorkingHour>?
)