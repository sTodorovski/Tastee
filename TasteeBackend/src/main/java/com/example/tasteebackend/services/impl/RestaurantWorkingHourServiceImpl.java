package com.example.tasteebackend.services.impl;

import com.example.tasteebackend.dto.RestaurantOrderStatusResponse;
import com.example.tasteebackend.dto.RestaurantWorkingHourRequest;
import com.example.tasteebackend.model.Restaurant;
import com.example.tasteebackend.model.RestaurantWorkingHour;
import com.example.tasteebackend.repository.RestaurantRepository;
import com.example.tasteebackend.repository.RestaurantWorkingHourRepository;
import com.example.tasteebackend.services.RestaurantWorkingHourService;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class RestaurantWorkingHourServiceImpl implements RestaurantWorkingHourService {

    private static final ZoneId RESTAURANT_ZONE = ZoneId.of("Europe/Skopje");

    private final RestaurantWorkingHourRepository repository;
    private final RestaurantRepository restaurantRepository;

    public RestaurantWorkingHourServiceImpl(
            RestaurantWorkingHourRepository repository,
            RestaurantRepository restaurantRepository
    ) {
        this.repository = repository;
        this.restaurantRepository = restaurantRepository;
    }

    @Override
    public List<RestaurantWorkingHour> getSchedule(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        return repository.findByRestaurantOrderByDayOfWeek(restaurant);
    }

    @Override
    public List<RestaurantWorkingHour> updateSchedule(
            Long restaurantId,
            List<RestaurantWorkingHourRequest> requests
    ) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        if (requests == null || requests.size() != 7) {
            throw new IllegalArgumentException("Working hours must contain exactly 7 days");
        }

        Set<DayOfWeek> days = new HashSet<>();

        for (RestaurantWorkingHourRequest request : requests) {
            if (request.getDayOfWeek() == null) {
                throw new IllegalArgumentException("Day of week is required");
            }

            DayOfWeek day = request.getDayOfWeek();

            if (!days.add(day)) {
                throw new IllegalArgumentException("Duplicate day: " + day);
            }

            if (Boolean.TRUE.equals(request.getClosed())) {
                request.setOpenTime(null);
                request.setCloseTime(null);
                continue;
            }

            if (request.getOpenTime() == null || request.getCloseTime() == null) {
                throw new IllegalArgumentException("Open and close time are required for " + day);
            }

            if (!request.getCloseTime().isAfter(request.getOpenTime())) {
                throw new IllegalArgumentException("Closing time must be after opening time for " + day);
            }
        }

        repository.deleteAll(repository.findByRestaurantOrderByDayOfWeek(restaurant));

        for (RestaurantWorkingHourRequest request : requests) {
            RestaurantWorkingHour workingHour = new RestaurantWorkingHour();
            workingHour.setRestaurant(restaurant);
            workingHour.setDayOfWeek(request.getDayOfWeek());

            boolean closed = Boolean.TRUE.equals(request.getClosed());
            workingHour.setClosed(closed);

            if (closed) {
                workingHour.setOpenTime(null);
                workingHour.setCloseTime(null);
            } else {
                workingHour.setOpenTime(request.getOpenTime());
                workingHour.setCloseTime(request.getCloseTime());
            }

            repository.save(workingHour);
        }

        return repository.findByRestaurantOrderByDayOfWeek(restaurant);
    }

    @Override
    public RestaurantOrderStatusResponse getCurrentOrderStatus(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        ZonedDateTime nowInRestaurantZone = ZonedDateTime.now(RESTAURANT_ZONE);
        DayOfWeek today = nowInRestaurantZone.getDayOfWeek();
        LocalTime now = nowInRestaurantZone.toLocalTime();

        List<RestaurantWorkingHour> schedule = repository.findByRestaurantOrderByDayOfWeek(restaurant);

        RestaurantWorkingHour todayHours = schedule.stream()
                .filter(hour -> hour.getDayOfWeek() == today)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Working hours not configured for " + today));

        boolean manuallyOpen = Boolean.TRUE.equals(restaurant.getOpen());
        boolean acceptingOrders = false;

        if (manuallyOpen &&
                !Boolean.TRUE.equals(todayHours.getClosed()) &&
                todayHours.getOpenTime() != null &&
                todayHours.getCloseTime() != null) {
            acceptingOrders = !now.isBefore(todayHours.getOpenTime()) && now.isBefore(todayHours.getCloseTime());
        }

        return new RestaurantOrderStatusResponse(
                manuallyOpen,
                acceptingOrders,
                today.name(),
                todayHours.getOpenTime() != null ? todayHours.getOpenTime().toString() : null,
                todayHours.getCloseTime() != null ? todayHours.getCloseTime().toString() : null
        );
    }
}