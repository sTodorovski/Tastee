package com.example.tastee.dto

data class RestaurantRatingSummaryDto(
    val averageRating: Double,
    val totalReviews: Int,
    val reviews: List<ReviewDto>
)