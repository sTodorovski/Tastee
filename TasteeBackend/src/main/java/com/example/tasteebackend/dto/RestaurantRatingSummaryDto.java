package com.example.tasteebackend.dto;

import java.util.List;

public class RestaurantRatingSummaryDto {
    private double averageRating;
    private int totalReviews;
    private List<ReviewDto> reviews;

    public RestaurantRatingSummaryDto(double averageRating, int totalReviews, List<ReviewDto> reviews) {
        this.averageRating = averageRating;
        this.totalReviews = totalReviews;
        this.reviews = reviews;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public int getTotalReviews() {
        return totalReviews;
    }

    public List<ReviewDto> getReviews() {
        return reviews;
    }
}