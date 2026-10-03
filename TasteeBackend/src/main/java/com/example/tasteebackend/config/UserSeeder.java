package com.example.tasteebackend.config;

import com.example.tasteebackend.model.User;
import com.example.tasteebackend.model.enums.Role;
import com.example.tasteebackend.repository.UserRepository;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Locale;
import java.util.Random;

@Component
@Order(1)
public class UserSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final Faker faker = new Faker(new Locale("en"));
    private final Random random = new Random();

    public UserSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        final int CUSTOMER_COUNT = 2500;
        final int OWNER_COUNT = 250;
        final int DELIVERY_COUNT = 1500;

        for (int i = 0; i < CUSTOMER_COUNT; i++) {
            String firstName = faker.name().firstName();
            String lastName = faker.name().lastName();

            User user = new User();
            user.setRole(Role.ROLE_CUSTOMER);
            user.setPassword(passwordEncoder.encode("password"));
            user.setUsername(generateUsername(firstName, lastName, i));
            user.setEmail(generateEmail(firstName, lastName, i));
            user.setName(firstName);
            user.setSurname(lastName);

            try {
                String profilePicture = ProfilePictureGenerator.generateProfilePicture(firstName, lastName, String.valueOf(i));
                user.setProfilePicture(profilePicture);
            } catch (IOException e) {
                e.printStackTrace();
                user.setProfilePicture(null);
            }

            userRepository.save(user);
        }

        for (int i = 0; i < DELIVERY_COUNT; i++) {
            String firstName = faker.name().firstName();
            String lastName = faker.name().lastName();

            User user = new User();
            user.setRole(Role.ROLE_DELIVERY);
            user.setPassword(passwordEncoder.encode("password"));
            user.setUsername(generateUsername(firstName, lastName, i));
            user.setEmail(generateEmail(firstName, lastName, i));
            user.setName(firstName);
            user.setSurname(lastName);

            try {
                String profilePicture = ProfilePictureGenerator.generateProfilePicture(firstName, lastName, "delivery_" + i);
                user.setProfilePicture(profilePicture);
            } catch (IOException e) {
                e.printStackTrace();
                user.setProfilePicture(null);
            }

            userRepository.save(user);
        }

        for (int i = 0; i < OWNER_COUNT; i++) {
            String firstName = faker.name().firstName();
            String lastName = faker.name().lastName();

            User owner = new User();
            owner.setRole(Role.ROLE_RESTAURANT);
            owner.setPassword(passwordEncoder.encode("password"));
            owner.setUsername(generateUsername(firstName, lastName, i + CUSTOMER_COUNT));
            owner.setEmail(generateEmail(firstName, lastName, i + CUSTOMER_COUNT));
            owner.setName(firstName);
            owner.setSurname(lastName);

            try {
                String profilePicture = ProfilePictureGenerator.generateProfilePicture(firstName, lastName, "owner_" + i);
                owner.setProfilePicture(profilePicture);
            } catch (IOException e) {
                e.printStackTrace();
                owner.setProfilePicture(null);
            }

            userRepository.save(owner);
        }
    }

    private String generateUsername(String first, String last, int number) {
        return first.toLowerCase() + "." + last.toLowerCase() + number;
    }

    private String generateEmail(String first, String last, int number) {
        String[] domains = {
                "gmail.com",
                "yahoo.com",
                "hotmail.com",
                "outlook.com"
        };

        return first.toLowerCase() + "." + last.toLowerCase() + number + "@" + domains[random.nextInt(domains.length)];
    }
}