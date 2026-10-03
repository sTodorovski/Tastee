package com.example.tastee.dto

import java.time.DayOfWeek

data class RestaurantWorkingHour(
    val id: Long?,
    val dayOfWeek: DayOfWeek,
    val openTime: String?,
    val closeTime: String?,
    val closed: Boolean
)