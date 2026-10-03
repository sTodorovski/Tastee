package com.example.tasteebackend.repository;

import com.example.tasteebackend.model.Restaurant;
import com.example.tasteebackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    Optional<Restaurant> findByName(String name);

    List<Restaurant> findByNameContainingIgnoreCase(String name);

    Optional<Restaurant> findByOwner(User owner);

    Optional<Restaurant> findByOwner_Id(Long ownerId);
}
