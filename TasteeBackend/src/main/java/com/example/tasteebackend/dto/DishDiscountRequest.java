package com.example.tasteebackend.dto;

import lombok.Data;

@Data
public class DishDiscountRequest {
    private Float discountPercentage;

    public void setDiscountPercentage(Float discountPercentage) {
        if (discountPercentage != null && (discountPercentage < 0f || discountPercentage >= 100f)) {
            throw new IllegalArgumentException("Discount percentage must be between 0 and less than 100.");
        }
        this.discountPercentage = discountPercentage;
    }
}