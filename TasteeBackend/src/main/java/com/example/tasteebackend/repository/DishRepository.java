package com.example.tasteebackend.repository;

import com.example.tasteebackend.model.Dish;
import com.example.tasteebackend.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DishRepository extends JpaRepository<Dish, Long> {
    List<Dish> findByRestaurant(Restaurant restaurant);
    List<Dish> findByCategory(String category);
    List<Dish> findByRestaurant_Id(Long restaurantId);
}