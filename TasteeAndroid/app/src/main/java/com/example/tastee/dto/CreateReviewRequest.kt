package com.example.tastee.dto

data class CreateReviewRequest(
    val rating: Int,
    val comment: String?
)