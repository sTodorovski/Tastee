package com.example.tasteebackend.services;

import com.example.tasteebackend.dto.RestaurantStatusRequest;
import com.example.tasteebackend.dto.RestaurantUpdateRequest;
import com.example.tasteebackend.model.Restaurant;

import java.util.List;

public interface RestaurantService {
    Restaurant createRestaurant(Restaurant restaurant);

    List<Restaurant> findAll();

    Restaurant findById(Long id);

    List<Restaurant> searchByName(String name);

    void deleteRestaurant(Long id);

    Restaurant updateRestaurant(Long id, RestaurantUpdateRequest request);

    Restaurant updateRestaurantStatus(Long id, RestaurantStatusRequest request);

    Restaurant findByOwnerId(Long ownerId);
}