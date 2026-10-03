package com.example.tasteebackend.services.impl;

import com.example.tasteebackend.dto.ReviewDto;
import com.example.tasteebackend.model.Review;
import com.example.tasteebackend.repository.ReviewRepository;
import com.example.tasteebackend.services.ReviewService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;

    public ReviewServiceImpl(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @Override
    public List<ReviewDto> getRestaurantReviews(Long restaurantId) {
        return reviewRepository.findByRestaurantIdOrderByIdDesc(restaurantId)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    private ReviewDto convertToDto(Review review) {
        return new ReviewDto(
                review.getId(),
                review.getRating(),
                review.getComment(),
                review.getUser().getUsername(),
                review.getUser().getName(),
                review.getUser().getSurname(),
                review.getUser().getProfilePicture()
        );
    }
}