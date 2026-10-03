package com.example.tasteebackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "restaurants")
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "phoneNumber", nullable = false, unique = true)
    private String phoneNumber;

    @Column(name = "location", nullable = false)
    private String location;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "workingHours", nullable = false)
    private String workingHours;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "logo")
    private String logo;

    @Column(name = "cover")
    private String cover;

    @Column(name = "open", nullable = false)
    private Boolean open = true;

    @JsonIgnore
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Review> reviewsList = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Dish> dishes = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RestaurantWorkingHour> workingHoursSchedule = new ArrayList<>();

    public Restaurant(
            String name,
            String email,
            String phoneNumber,
            String location,
            User owner,
            String workingHours,
            String description,
            String logo,
            String cover
    ) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.location = location;
        this.owner = owner;
        this.workingHours = workingHours;
        this.description = description;
        this.logo = logo;
        this.cover = cover;
        this.open = true;
    }

    public Restaurant() {
        this.open = true;
    }

    public Double getAverageRating() {
        if (reviewsList == null || reviewsList.isEmpty()) {
            return 0.0;
        }
        return reviewsList.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }

    public Integer getReviewCount() {
        return reviewsList != null ? reviewsList.size() : 0;
    }
}