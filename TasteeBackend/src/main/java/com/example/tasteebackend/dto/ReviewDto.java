package com.example.tasteebackend.dto;

public record ReviewDto(
        Long id,
        Integer rating,
        String comment,
        String username,
        String name,
        String surname,
        String profilePicture
) {
}