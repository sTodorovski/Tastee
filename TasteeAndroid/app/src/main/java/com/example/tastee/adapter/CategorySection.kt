package com.example.tastee.adapter

import com.example.tastee.dto.Restaurant

data class CategorySection(
    val categoryName: String,
    val restaurants: List<CategoryRestaurant>
)

data class CategoryRestaurant(
    val restaurant: Restaurant,
    val dishCount: Int
)