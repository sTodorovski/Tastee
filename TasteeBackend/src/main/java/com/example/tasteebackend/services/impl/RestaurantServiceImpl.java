package com.example.tasteebackend.services.impl;

import com.example.tasteebackend.dto.RestaurantStatusRequest;
import com.example.tasteebackend.dto.RestaurantUpdateRequest;
import com.example.tasteebackend.model.Restaurant;
import com.example.tasteebackend.repository.RestaurantRepository;
import com.example.tasteebackend.services.RestaurantService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public RestaurantServiceImpl(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    @Override
    public Restaurant createRestaurant(Restaurant restaurant) {
        if (restaurant.getOpen() == null) {
            restaurant.setOpen(true);
        }
        return restaurantRepository.save(restaurant);
    }

    @Override
    public List<Restaurant> findAll() {
        return restaurantRepository.findAll();
    }

    @Override
    public Restaurant findById(Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));
    }

    @Override
    public List<Restaurant> searchByName(String name) {
        return restaurantRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    public void deleteRestaurant(Long id) {
        restaurantRepository.deleteById(id);
    }

    @Override
    public Restaurant updateRestaurant(Long id, RestaurantUpdateRequest request) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        restaurant.setName(request.getName());
        restaurant.setEmail(request.getEmail());
        restaurant.setPhoneNumber(request.getPhoneNumber());
        restaurant.setLocation(request.getLocation());
        restaurant.setWorkingHours(request.getWorkingHours());
        restaurant.setDescription(request.getDescription());
        restaurant.setLogo(request.getLogo());
        restaurant.setCover(request.getCover());

        return restaurantRepository.save(restaurant);
    }

    @Override
    public Restaurant updateRestaurantStatus(Long id, RestaurantStatusRequest request) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        if (request.getOpen() == null) {
            throw new IllegalArgumentException("Restaurant status cannot be null");
        }

        restaurant.setOpen(request.getOpen());
        return restaurantRepository.save(restaurant);
    }

    @Override
    public Restaurant findByOwnerId(Long ownerId) {
        return restaurantRepository.findByOwner_Id(ownerId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found for this owner"));
    }
}