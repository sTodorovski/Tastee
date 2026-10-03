package com.example.tasteebackend.repository;

import com.example.tasteebackend.model.Restaurant;
import com.example.tasteebackend.model.RestaurantWorkingHour;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RestaurantWorkingHourRepository extends JpaRepository<RestaurantWorkingHour, Long> {
    List<RestaurantWorkingHour> findByRestaurantOrderByDayOfWeek(Restaurant restaurant);
}