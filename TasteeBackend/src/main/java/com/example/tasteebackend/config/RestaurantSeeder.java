package com.example.tasteebackend.config;

import com.example.tasteebackend.model.Restaurant;
import com.example.tasteebackend.model.Review;
import com.example.tasteebackend.model.User;
import com.example.tasteebackend.model.enums.Role;
import com.example.tasteebackend.repository.RestaurantRepository;
import com.example.tasteebackend.repository.ReviewRepository;
import com.example.tasteebackend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@Order(2)
public class RestaurantSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final ReviewRepository reviewRepository;

    public RestaurantSeeder(
            UserRepository userRepository,
            RestaurantRepository restaurantRepository,
            ReviewRepository reviewRepository
    ) {
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.reviewRepository = reviewRepository;
    }

    record RestaurantJson(
            String name,
            double latitude,
            double longitude,
            String phone,
            String website
    ) {}

    @Override
    public void run(String... args) throws Exception {
        if (restaurantRepository.count() > 0) {
            return;
        }

        List<User> owners = userRepository.findByRole(Role.ROLE_RESTAURANT);
        List<User> customers = userRepository.findByRole(Role.ROLE_CUSTOMER);

        if (owners.isEmpty()) {
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("restaurants.json");

        if (inputStream == null) {
            throw new RuntimeException("restaurants.json not found in resources folder");
        }

        List<RestaurantJson> restaurants = mapper.readValue(
                inputStream,
                new TypeReference<List<RestaurantJson>>() {}
        );

        Random random = new Random();

        for (RestaurantJson data : restaurants) {
            if (data.name() == null || data.name().isBlank()) {
                continue;
            }

            Restaurant restaurant = new Restaurant();
            restaurant.setName(data.name());
            restaurant.setPhoneNumber(
                    data.phone() == null || data.phone().isBlank()
                            ? generatePhone(random)
                            : data.phone()
            );
            restaurant.setEmail(generateEmail(data.name()));
            restaurant.setLocation(data.latitude() + ", " + data.longitude());
            restaurant.setWorkingHours("08:00-22:00");
            restaurant.setDescription("No description available.");
            restaurant.setOwner(owners.get(random.nextInt(owners.size())));

            try {
                restaurant.setLogo(LogoGenerator.generateLogo(data.name()));
            } catch (Exception e) {
                e.printStackTrace();
                restaurant.setLogo("/images/default_logo.png");
            }

            restaurant.setCover(randomCover());
            restaurantRepository.save(restaurant);

            List<User> shuffledCustomers = new ArrayList<>(customers);
            java.util.Collections.shuffle(shuffledCustomers, random);

            int reviewCount = Math.min(
                    10 + random.nextInt(100),
                    shuffledCustomers.size()
            );

            for (int i = 0; i < reviewCount; i++) {
                User customer = shuffledCustomers.get(i);
                int rating = 1 + random.nextInt(5);
                Review review = new Review(
                        customer,
                        restaurant,
                        rating,
                        generateRandomReview(rating)
                );
                reviewRepository.save(review);
            }
        }
    }

    private String generatePhone(Random random) {
        return "07" + (1000000 + random.nextInt(9000000));
    }

    private String generateRandomReview(int rating) {
        String[] positiveReviews = {
                "Great food and excellent service!",
                "Really enjoyed the meal. Would definitely come back.",
                "Amazing food and a great atmosphere.",
                "Everything was delicious and fresh.",
                "One of the best restaurants I've visited.",
                "The food was fantastic and the staff were friendly.",
                "The food was awesome!",
                "The staff are very friendly and helpful.",
                "Very delicious food and great view.",
                "Will definetely come back here again.",
                "Do not skip this restaurant.",
                "The ambiance was great and the food was divine.",
                "Very tasty food. Will recommend to all my friends.",
                "The great reviews made me check this place out. Did not disappoint.",
                "Lovely!",
                "Mmm. Tasty!",
                "Will definitely recommend to everyone I know.",
                "Wow!",
                "Awesome!",
                "The service is very good.",
                "The meat is so juicy and tender here.",
                "Very delicious dinner options!",
                "Do not skip this place.",
                "A worthwhile trip to the restaurant for any tourist."
        };

        String[] neutralReviews = {
                "The food was good and the service was okay.",
                "A decent place for a quick meal.",
                "Overall a pleasant experience.",
                "The food was nice, but there is room for improvement.",
                "Pretty good experience overall.",
                "It was okay.",
                "Eh.",
                "Okay.",
                "Could've been better.",
                "At least it was pretty cheap.",
                "Could've been cheaper.",
                "The meat is okay.",
                "Not a lot of options for vegans.",
                "The staff aren't very friendly, but at least the food is okay.",
                "The meat was kind of underwhelming.",
                "Could have been worse.",
                "You take what you can get.",
                "What can you expect from a cheap restaurant.",
                "Nothing spectacular.",
                "Average.",
                "Average quality."
        };

        String[] negativeReviews = {
                "The food was disappointing.",
                "The service could have been much better.",
                "Unfortunately, I was not very impressed.",
                "The food was okay, but I expected more.",
                "Not the best experience I've had.",
                "Bad.",
                "Very bad.",
                "Terrible.",
                "Blegh.",
                "Will not recommend.",
                "Could have been much better.",
                "Not worth it.",
                "Very expensive and not very good.",
                "The staff are very rude.",
                "The food was delayed twice.",
                "Terrible customer service.",
                "Do not come here.",
                "Skip.",
                "Horrible.",
                "Awful.",
                "The food made me vomit."
        };

        Random random = new Random();

        if (rating >= 4) {
            return positiveReviews[random.nextInt(positiveReviews.length)];
        } else if (rating == 3) {
            return neutralReviews[random.nextInt(neutralReviews.length)];
        } else {
            return negativeReviews[random.nextInt(negativeReviews.length)];
        }
    }

    private String generateEmail(String name) {
        return name
                .toLowerCase()
                .replace(" ", "")
                .replace(",", "")
                .replace("'", "")
                + "@tastee.mk";
    }

    private String randomCover() {
        Random random = new Random();
        int number = 1 + random.nextInt(20);
        return "/images/covers/cover" + number + ".jpg";
    }
}