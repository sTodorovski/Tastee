package com.example.tastee.dto

data class UpdateUserRequest(
    val username: String,
    val email: String,
    val name: String,
    val surname: String
)