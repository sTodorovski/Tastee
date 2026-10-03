package com.example.tastee.dto

data class ReviewDto(
    val id: Long,
    val rating: Int,
    val comment: String?,
    val username: String?,
    val name: String?,
    val surname: String?,
    val profilePicture: String?
)