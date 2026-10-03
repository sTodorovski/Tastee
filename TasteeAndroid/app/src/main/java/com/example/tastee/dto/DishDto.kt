package com.example.tastee.dto

data class Dish(
    val id: Long,
    val name: String,
    val image: String?,
    val price: Float,
    val category: String,
    val restaurantId: Long?,
    val description: String?,
    val prepTime: Int,
    val vegetarian: Boolean?,
    val vegan: Boolean?,
    val spicy: Boolean?,
    val discountPercentage: Float?,
    val availability: Boolean
)