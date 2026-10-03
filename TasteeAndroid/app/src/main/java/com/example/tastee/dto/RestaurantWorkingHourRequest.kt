package com.example.tastee.dto

import java.time.DayOfWeek

data class RestaurantWorkingHourRequest(
    val dayOfWeek: DayOfWeek,
    val openTime: String?,
    val closeTime: String?,
    val closed: Boolean
)