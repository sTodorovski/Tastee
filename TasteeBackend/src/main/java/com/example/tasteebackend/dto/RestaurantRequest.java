package com.example.tasteebackend.dto;

import lombok.Data;

@Data
public class RestaurantRequest {
    private String name;
    private String email;
    private String phoneNumber;
    private String location;
    private String workingHours;
    private String description;
    private String logo;
    private String cover;
}