package com.example.tasteebackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "dish")
public class Dish {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "image", nullable = false, columnDefinition = "TEXT")
    private String image;

    @Column(name = "price", nullable = false)
    private float price;

    @Column(name = "category", nullable = false)
    private String category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    @JsonIgnore
    private Restaurant restaurant;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "prepTime")
    private int prepTime;

    private Boolean availability;
    private Boolean vegetarian;
    private Boolean vegan;
    private Boolean spicy;
    private Float discountPercentage;

    public Dish() {
    }

    public Dish(String name, String image, float price, String category, Restaurant restaurant, String description, int prepTime) {
        this.name = name;
        this.image = image;
        this.price = price;
        this.category = category;
        this.restaurant = restaurant;
        this.description = description;
        this.prepTime = prepTime;
    }

    @JsonProperty("restaurantId")
    public Long getRestaurantId() {
        return restaurant != null ? restaurant.getId() : null;
    }

    public Boolean getAvailability() {
        return availability;
    }

    public void setAvailability(Boolean availability) {
        this.availability = availability;
    }
}