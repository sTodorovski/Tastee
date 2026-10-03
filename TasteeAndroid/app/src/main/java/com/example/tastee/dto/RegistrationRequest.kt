package com.example.tastee.dto

data class RegistrationRequest(
    val username: String,
    val email: String,
    val password: String,
    val repeatPassword: String,
    val name: String,
    val surname: String,
    val role: String
)