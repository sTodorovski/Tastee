package com.example.tasteebackend.services;

import com.example.tasteebackend.dto.ReviewDto;

import java.util.List;

public interface ReviewService {
    List<ReviewDto> getRestaurantReviews(Long restaurantId);
}