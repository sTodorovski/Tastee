package com.example.tasteebackend.services.impl;

import com.example.tasteebackend.model.Dish;
import com.example.tasteebackend.repository.DishRepository;
import com.example.tasteebackend.services.DishService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DishServiceImpl implements DishService {

    private final DishRepository dishRepository;

    public DishServiceImpl(DishRepository dishRepository) {
        this.dishRepository = dishRepository;
    }

    @Override
    public List<Dish> getAllDishes() {
        return dishRepository.findAll();
    }

    @Override
    public Dish getDishById(Long id) {
        return dishRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dish not found"));
    }

    @Override
    public Dish createDish(Dish dish) {
        return dishRepository.save(dish);
    }

    @Override
    public Dish updateDish(Long id, Dish updatedDish) {
        Dish dish = getDishById(id);
        dish.setName(updatedDish.getName());
        dish.setImage(updatedDish.getImage());
        dish.setPrice(updatedDish.getPrice());
        dish.setCategory(updatedDish.getCategory());
        dish.setDescription(updatedDish.getDescription());
        dish.setPrepTime(updatedDish.getPrepTime());
        dish.setVegetarian(updatedDish.getVegetarian());
        dish.setVegan(updatedDish.getVegan());
        dish.setSpicy(updatedDish.getSpicy());
        return dishRepository.save(dish);
    }

    @Override
    public void deleteDish(Long id) {
        dishRepository.deleteById(id);
    }

    @Override
    public List<Dish> getDishesByRestaurant(Long restaurantId) {
        return dishRepository.findByRestaurant_Id(restaurantId);
    }

    @Override
    public List<Dish> getDishesByCategory(String category) {
        return dishRepository.findByCategory(category);
    }

    @Override
    public Dish updateAvailability(Long id, Boolean availability) {
        Dish dish = getDishById(id);
        dish.setAvailability(availability);
        return dishRepository.save(dish);
    }

    @Override
    public Dish updateDiscount(Long id, Float discountPercentage) {
        Dish dish = getDishById(id);
        dish.setDiscountPercentage(discountPercentage);
        return dishRepository.save(dish);
    }
}