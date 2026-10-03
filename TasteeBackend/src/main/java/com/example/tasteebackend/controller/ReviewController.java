package com.example.tasteebackend.controller;

import com.example.tasteebackend.dto.ReviewDto;
import com.example.tasteebackend.model.Review;
import com.example.tasteebackend.model.Restaurant;
import com.example.tasteebackend.model.User;
import com.example.tasteebackend.repository.RestaurantRepository;
import com.example.tasteebackend.repository.ReviewRepository;
import com.example.tasteebackend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/restaurants")
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    public ReviewController(
            ReviewRepository reviewRepository,
            RestaurantRepository restaurantRepository,
            UserRepository userRepository
    ) {
        this.reviewRepository = reviewRepository;
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/{restaurantId}/reviews")
    public List<ReviewDto> getRestaurantReviews(@PathVariable Long restaurantId) {
        return reviewRepository
                .findByRestaurantIdOrderByIdDesc(restaurantId)
                .stream()
                .map(review -> new ReviewDto(
                        review.getId(),
                        review.getRating(),
                        review.getComment(),
                        review.getUser().getUsername(),
                        review.getUser().getName(),
                        review.getUser().getSurname(),
                        review.getUser().getProfilePicture()
                ))
                .toList();
    }

    @PostMapping("/{restaurantId}/reviews")
    public ResponseEntity<?> addReview(
            @PathVariable Long restaurantId,
            @RequestBody CreateReviewRequest request,
            Authentication authentication
    ) {
        if (request.rating() == null || request.rating() < 1 || request.rating() > 5) {
            return ResponseEntity.badRequest().body("Rating must be between 1 and 5");
        }

        Restaurant restaurant = restaurantRepository.findById(restaurantId).orElse(null);

        if (restaurant == null) {
            return ResponseEntity.notFound().build();
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        Review review = new Review(
                user,
                restaurant,
                request.rating(),
                request.comment()
        );

        Review savedReview = reviewRepository.save(review);

        return ResponseEntity.ok(
                new ReviewDto(
                        savedReview.getId(),
                        savedReview.getRating(),
                        savedReview.getComment(),
                        savedReview.getUser().getUsername(),
                        savedReview.getUser().getName(),
                        savedReview.getUser().getSurname(),
                        savedReview.getUser().getProfilePicture()
                )
        );
    }

    public record CreateReviewRequest(
            Integer rating,
            String comment
    ) {}
}