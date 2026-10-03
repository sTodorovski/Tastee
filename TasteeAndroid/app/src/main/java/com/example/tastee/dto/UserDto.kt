package com.example.tastee.dto

data class UserDto(
    val id: Long?,
    val username: String,
    val email: String,
    val name: String?,
    val surname: String?,
    val profilePicture: String?,
    val role: String
)