package com.example.tastee.fragment

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tastee.R
import com.example.tastee.RestaurantActivity
import com.example.tastee.adapter.CategoryAdapter
import com.example.tastee.adapter.CategoryRestaurant
import com.example.tastee.adapter.CategorySection
import com.example.tastee.dto.Dish
import com.example.tastee.dto.Restaurant
import com.example.tastee.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.tastee.utils.LocationManager

class CategoriesFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CategoryAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_categories, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.categoryRecyclerView)
        adapter = CategoryAdapter(emptyList()) { restaurant -> openRestaurant(restaurant) }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        loadCategories()
    }

    private fun loadCategories() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val restaurants = RetrofitClient.restaurantApi.getRestaurants()
                val dishes = RetrofitClient.restaurantApi.getAllDishes()
                val userLocation = if (LocationManager.locationSortingEnabled) {
                    LocationManager.userLocation
                } else { null }
                val categories = groupRestaurantsByCategory(restaurants, dishes, userLocation)
                withContext(Dispatchers.Main) {
                    adapter.updateCategories(categories, userLocation)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (!isAdded) {
                        return@withContext
                    }
                    Toast.makeText(requireContext(), e.message ?: "Failed loading categories", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun groupRestaurantsByCategory(restaurants: List<Restaurant>, dishes: List<Dish>, userLocation: Location?): List<CategorySection> {
        val restaurantDishes = dishes.groupBy { it.restaurantId }
        val categoryMap = mutableMapOf<String, MutableList<CategoryRestaurant>>()
        for (restaurant in restaurants) {
            val dishesForRestaurant = restaurantDishes[restaurant.id] ?: emptyList()
            if (dishesForRestaurant.isEmpty()) {
                continue
            }
            val categoryCounts = dishesForRestaurant.filter { it.category.isNotBlank() }.groupingBy { it.category }.eachCount()
            if (categoryCounts.isEmpty()) {
                continue
            }
            val maxCount = categoryCounts.values.maxOrNull() ?: continue
            val dominantCategories = categoryCounts.filter { it.value == maxCount }
            for ((category, count) in dominantCategories) {
                val categoryRestaurant = CategoryRestaurant(restaurant = restaurant, dishCount = count)
                categoryMap.getOrPut(category) { mutableListOf() }.add(categoryRestaurant)
            }
        }
        return categoryMap.map { (category, restaurantsInCategory) ->
            val sortedRestaurants = if (userLocation != null) {
                restaurantsInCategory.mapNotNull { categoryRestaurant ->
                    val restaurantLocation = parseCoordinates(categoryRestaurant.restaurant.location)
                    if (restaurantLocation != null) {
                        val distance = userLocation.distanceTo(restaurantLocation)
                        categoryRestaurant to distance
                    } else {
                        null
                    }
                }.sortedBy { it.second }.map { it.first }
            } else {
                restaurantsInCategory.sortedByDescending { it.dishCount }
            }
            CategorySection(categoryName = category, restaurants = sortedRestaurants)
        }.sortedBy { it.categoryName }
    }

    private fun openRestaurant(restaurant: Restaurant) {
        val intent = Intent(requireContext(), RestaurantActivity::class.java)
        intent.putExtra("restaurantId", restaurant.id)
        startActivity(intent)
    }

    private fun parseCoordinates(location: String?): Location? {
        if (location.isNullOrBlank()) {
            return null
        }
        return try {
            val parts = location.split(",")
            if (parts.size != 2) {
                return null
            }
            val latitude = parts[0].trim().toDouble()
            val longitude = parts[1].trim().toDouble()
            if (latitude !in -90.0..90.0 || longitude !in -180.0..180.0) {
                return null
            }
            Location("restaurant").apply {
                this.latitude = latitude
                this.longitude = longitude
            }
        } catch (e: Exception) {
            null
        }
    }
}