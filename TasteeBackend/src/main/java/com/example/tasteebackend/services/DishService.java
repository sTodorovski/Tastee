package com.example.tasteebackend.services;

import com.example.tasteebackend.model.Dish;

import java.util.List;

public interface DishService {
    List<Dish> getAllDishes();

    Dish getDishById(Long id);

    Dish createDish(Dish dish);

    Dish updateDish(Long id, Dish dish);

    void deleteDish(Long id);

    List<Dish> getDishesByRestaurant(Long restaurantId);

    List<Dish> getDishesByCategory(String category);

    Dish updateAvailability(Long id, Boolean availability);

    Dish updateDiscount(Long id, Float discountPercentage);
}