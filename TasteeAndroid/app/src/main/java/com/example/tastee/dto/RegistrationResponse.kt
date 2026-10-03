package com.example.tastee.dto

data class RegistrationResponse(
    val id: Long,
    val username: String,
    val email: String,
    val role: String
)