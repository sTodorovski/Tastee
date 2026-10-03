package com.example.tasteebackend.dto;

public record UpdateUserRequest(
        String username,
        String email,
        String name,
        String surname
) {
}