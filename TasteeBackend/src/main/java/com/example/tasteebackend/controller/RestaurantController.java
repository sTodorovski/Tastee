package com.example.tasteebackend.controller;

import com.example.tasteebackend.dto.RestaurantStatusRequest;
import com.example.tasteebackend.dto.RestaurantUpdateRequest;
import com.example.tasteebackend.model.Restaurant;
import com.example.tasteebackend.model.User;
import com.example.tasteebackend.repository.UserRepository;
import com.example.tasteebackend.services.RestaurantService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.example.tasteebackend.dto.RestaurantWorkingHourRequest;
import com.example.tasteebackend.model.RestaurantWorkingHour;
import com.example.tasteebackend.services.RestaurantWorkingHourService;
import com.example.tasteebackend.dto.RestaurantOrderStatusResponse;
import com.example.tasteebackend.model.Dish;
import com.example.tasteebackend.services.DishService;

import java.util.List;

@RestController
@RequestMapping("/restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final UserRepository userRepository;
    private final RestaurantWorkingHourService workingHourService;
    private final DishService dishService;

    public RestaurantController(
            RestaurantService restaurantService,
            UserRepository userRepository,
            RestaurantWorkingHourService workingHourService,
            DishService dishService
    ) {
        this.restaurantService = restaurantService;
        this.userRepository = userRepository;
        this.workingHourService = workingHourService;
        this.dishService = dishService;
    }

    @PostMapping
    public ResponseEntity<Restaurant> createRestaurant(
            @RequestBody Restaurant restaurant,
            Authentication authentication
    ) {
        User owner = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        restaurant.setOwner(owner);
        Restaurant saved = restaurantService.createRestaurant(restaurant);

        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<List<Restaurant>> getRestaurants() {
        return ResponseEntity.ok(restaurantService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Restaurant> getRestaurant(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.findById(id));
    }

    @GetMapping("/{id}/dishes")
    public ResponseEntity<List<Dish>> getRestaurantDishes(@PathVariable Long id) {
        return ResponseEntity.ok(dishService.getDishesByRestaurant(id));
    }

    @GetMapping("/mine")
    public ResponseEntity<Restaurant> getMyRestaurant(Authentication authentication) {
        User owner = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        return ResponseEntity.ok(restaurantService.findByOwnerId(owner.getId()));
    }

    @PutMapping("/mine")
    public ResponseEntity<Restaurant> updateMyRestaurant(
            @RequestBody RestaurantUpdateRequest request,
            Authentication authentication
    ) {
        User owner = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        Restaurant restaurant = restaurantService.findByOwnerId(owner.getId());
        Restaurant updated = restaurantService.updateRestaurant(restaurant.getId(), request);

        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/mine/status")
    public ResponseEntity<Restaurant> updateMyRestaurantStatus(
            @RequestBody RestaurantStatusRequest request,
            Authentication authentication
    ) {
        User owner = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        Restaurant restaurant = restaurantService.findByOwnerId(owner.getId());
        Restaurant updated = restaurantService.updateRestaurantStatus(restaurant.getId(), request);

        return ResponseEntity.ok(updated);
    }

    @GetMapping("/mine/working-hours")
    public ResponseEntity<List<RestaurantWorkingHour>> getMyWorkingHours(Authentication authentication) {
        User owner = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        Restaurant restaurant = restaurantService.findByOwnerId(owner.getId());

        return ResponseEntity.ok(workingHourService.getSchedule(restaurant.getId()));
    }

    @PutMapping("/mine/working-hours")
    public ResponseEntity<List<RestaurantWorkingHour>> updateMyWorkingHours(
            @RequestBody List<RestaurantWorkingHourRequest> requests,
            Authentication authentication
    ) {
        User owner = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        Restaurant restaurant = restaurantService.findByOwnerId(owner.getId());

        return ResponseEntity.ok(workingHourService.updateSchedule(restaurant.getId(), requests));
    }

    @GetMapping("/{id}/working-hours")
    public ResponseEntity<List<RestaurantWorkingHour>> getRestaurantWorkingHours(@PathVariable Long id) {
        return ResponseEntity.ok(workingHourService.getSchedule(id));
    }

    @GetMapping("/{id}/order-status")
    public ResponseEntity<RestaurantOrderStatusResponse> getRestaurantOrderStatus(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(workingHourService.getCurrentOrderStatus(id));
        } catch (Exception e) {
            return ResponseEntity.ok(new RestaurantOrderStatusResponse(true, true, "TODAY", null, null));
        }
    }
}