package com.example.tasteebackend.controller;

import com.example.tasteebackend.dto.DishAvailabilityRequest;
import com.example.tasteebackend.dto.DishDiscountRequest;
import com.example.tasteebackend.dto.DishRequest;
import com.example.tasteebackend.model.Dish;
import com.example.tasteebackend.model.Restaurant;
import com.example.tasteebackend.model.User;
import com.example.tasteebackend.repository.RestaurantRepository;
import com.example.tasteebackend.services.DishService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/dishes")
@CrossOrigin
public class DishController {

    private final DishService dishService;
    private final RestaurantRepository restaurantRepository;

    public DishController(DishService dishService, RestaurantRepository restaurantRepository) {
        this.dishService = dishService;
        this.restaurantRepository = restaurantRepository;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path path = Paths.get("uploads/" + fileName);
            Files.createDirectories(path.getParent());
            Files.write(path, file.getBytes());

            return ResponseEntity.ok("/images/" + fileName);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Dish>> getAllDishes() {
        return ResponseEntity.ok(dishService.getAllDishes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Dish> getDishById(@PathVariable Long id) {
        return ResponseEntity.ok(dishService.getDishById(id));
    }

    @PostMapping
    public ResponseEntity<Dish> createDish(@RequestBody DishRequest request, Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        Restaurant restaurant = restaurantRepository
                .findByOwner_Id(user.getId())
                .orElseThrow(() -> new RuntimeException("You do not own a restaurant"));

        if (!restaurant.getId().equals(request.getRestaurantId())) {
            return ResponseEntity.status(403).build();
        }

        Dish dish = new Dish();
        dish.setName(request.getName());
        dish.setImage(request.getImage());
        dish.setPrice(request.getPrice());
        dish.setCategory(request.getCategory());
        dish.setDescription(request.getDescription());
        dish.setPrepTime(request.getPrepTime());
        dish.setVegetarian(request.getVegetarian());
        dish.setVegan(request.getVegan());
        dish.setSpicy(request.getSpicy());
        dish.setDiscountPercentage(request.getDiscountPercentage());
        dish.setRestaurant(restaurant);

        return ResponseEntity.ok(dishService.createDish(dish));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Dish> updateDish(@PathVariable Long id, @RequestBody Dish dish) {
        return ResponseEntity.ok(dishService.updateDish(id, dish));
    }

    @PatchMapping("/{id}/availability")
    public ResponseEntity<Dish> updateAvailability(
            @PathVariable Long id,
            @RequestBody DishAvailabilityRequest request,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        Restaurant restaurant = restaurantRepository
                .findByOwner_Id(user.getId())
                .orElseThrow(() -> new RuntimeException("You do not own a restaurant"));

        Dish dish = dishService.getDishById(id);

        if (dish.getRestaurant() == null || !dish.getRestaurant().getId().equals(restaurant.getId())) {
            return ResponseEntity.status(403).build();
        }

        if (request.getAvailability() == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(dishService.updateAvailability(id, request.getAvailability()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDish(@PathVariable Long id) {
        dishService.deleteDish(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<Dish>> getRestaurantDishes(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(dishService.getDishesByRestaurant(restaurantId));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Dish>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(dishService.getDishesByCategory(category));
    }

    @PatchMapping("/{id}/discount")
    public ResponseEntity<Dish> updateDiscount(
            @PathVariable Long id,
            @RequestBody DishDiscountRequest request,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        Restaurant restaurant = restaurantRepository
                .findByOwner_Id(user.getId())
                .orElseThrow(() -> new RuntimeException("You do not own a restaurant"));

        Dish dish = dishService.getDishById(id);

        if (dish.getRestaurant() == null || !dish.getRestaurant().getId().equals(restaurant.getId())) {
            return ResponseEntity.status(403).build();
        }

        Float discount = request.getDiscountPercentage();

        if (discount != null && (discount < 0 || discount > 100)) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(dishService.updateDiscount(id, discount));
    }
}