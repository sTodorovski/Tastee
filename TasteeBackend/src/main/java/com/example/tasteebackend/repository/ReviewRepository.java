package com.example.tasteebackend.repository;

import com.example.tasteebackend.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByRestaurantIdOrderByIdDesc(Long restaurantId);

    long countByRestaurantId(Long restaurantId);

    boolean existsByUserIdAndRestaurantId(Long userId, Long restaurantId);

    @Query("""
        SELECT AVG(r.rating)
        FROM Review r
        WHERE r.restaurant.id = :restaurantId
    """)
    Double getAverageRating(@Param("restaurantId") Long restaurantId);
}