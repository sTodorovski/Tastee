package com.example.tasteebackend.config;

import com.example.tasteebackend.model.Dish;
import com.example.tasteebackend.model.Restaurant;
import com.example.tasteebackend.repository.DishRepository;
import com.example.tasteebackend.repository.RestaurantRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@Order(3)
public class DishSeeder implements CommandLineRunner {

    private final DishRepository dishRepository;
    private final RestaurantRepository restaurantRepository;

    public DishSeeder(
            DishRepository dishRepository,
            RestaurantRepository restaurantRepository
    ) {
        this.dishRepository = dishRepository;
        this.restaurantRepository = restaurantRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (dishRepository.count() > 0) {
            return;
        }

        List<Restaurant> restaurants = restaurantRepository.findAll();

        if (restaurants.isEmpty()) {
            return;
        }

        List<JsonNode> meals = fetchMeals();

        if (meals.isEmpty()) {
            return;
        }

        Random random = new Random();

        for (Restaurant restaurant : restaurants) {
            int amount = 10 + random.nextInt(10);

            for (int i = 0; i < amount; i++) {
                JsonNode meal = meals.get(random.nextInt(meals.size()));

                Dish dish = new Dish();

                dish.setName(meal.path("strMeal").asText());
                dish.setCategory(meal.path("strCategory").asText("Food"));
                dish.setDescription(meal.path("strInstructions").asText("No description available."));
                dish.setImage(meal.path("strMealThumb").asText("default_food.jpg"));
                dish.setPrice(random.nextInt(15) + 3 + 0.99f);
                dish.setPrepTime(10 + (random.nextInt(4) * 5));
                dish.setAvailability(true);
                dish.setVegetarian(random.nextBoolean());
                dish.setVegan(random.nextBoolean());
                dish.setSpicy(random.nextBoolean());
                dish.setDiscountPercentage(Math.round(random.nextFloat() * 30 * 10) / 10f);
                dish.setRestaurant(restaurant);

                dishRepository.save(dish);
            }
        }
    }

    private List<JsonNode> fetchMeals() throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper mapper = new ObjectMapper();

        String[] searches = {"chicken", "pizza", "pasta", "beef", "fish", "dessert", "rice", "vegetable", "burger"};

        List<JsonNode> result = new ArrayList<>();

        for (String search : searches) {
            String response = restTemplate.getForObject("https://www.themealdb.com/api/json/v1/1/search.php?s=" + search, String.class);

            JsonNode root = mapper.readTree(response);
            JsonNode meals = root.path("meals");

            if (meals.isArray()) {
                for (JsonNode meal : meals) {
                    result.add(meal);
                }
            }
        }

        return result;
    }
}