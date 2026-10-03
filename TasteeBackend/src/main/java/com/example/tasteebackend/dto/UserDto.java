package com.example.tasteebackend.dto;

public record UserDto(
        Long id,
        String username,
        String email,
        String name,
        String surname,
        String profilePicture,
        String role
) {
}