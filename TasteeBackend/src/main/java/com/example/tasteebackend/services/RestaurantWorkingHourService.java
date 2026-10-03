package com.example.tasteebackend.services;

import com.example.tasteebackend.dto.RestaurantOrderStatusResponse;
import com.example.tasteebackend.dto.RestaurantWorkingHourRequest;
import com.example.tasteebackend.model.RestaurantWorkingHour;

import java.util.List;

public interface RestaurantWorkingHourService {
    List<RestaurantWorkingHour> getSchedule(Long restaurantId);

    List<RestaurantWorkingHour> updateSchedule(Long restaurantId, List<RestaurantWorkingHourRequest> requests);

    RestaurantOrderStatusResponse getCurrentOrderStatus(Long restaurantId);
}