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
import com.example.tastee.utils.LocationManager
import com.example.tastee.R
import com.example.tastee.RestaurantActivity
import com.example.tastee.Searchable
import com.example.tastee.adapter.RestaurantAdapter
import com.example.tastee.dto.Restaurant
import com.example.tastee.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RestaurantsFragment : Fragment(), Searchable {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RestaurantAdapter
    private var allRestaurants: List<Restaurant> = emptyList()
    private var currentSearchQuery: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_restaurants, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.restaurantRecyclerView)
        adapter = RestaurantAdapter(emptyList()) { restaurant ->
            val intent = Intent(requireContext(), RestaurantActivity::class.java)
            intent.putExtra("restaurantId", restaurant.id)
            startActivity(intent)
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        val userLocation = LocationManager.userLocation
        if (LocationManager.locationSortingEnabled && userLocation != null) {
            adapter.updateUserLocation(userLocation.latitude, userLocation.longitude)
        }

        loadRestaurants()
    }

    override fun onSearchQuery(query: String) {
        currentSearchQuery = query
        applyFilter()
    }

    private fun applyFilter() {
        val filteredList = if (currentSearchQuery.isBlank()) {
            allRestaurants
        } else {
            allRestaurants.filter { restaurant ->
                restaurant.name.contains(currentSearchQuery, ignoreCase = true) ||
                        (restaurant.description?.contains(currentSearchQuery, ignoreCase = true) == true)
            }
        }
        adapter.updateRestaurants(filteredList)
    }

    private fun loadRestaurants() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val restaurants = RetrofitClient.restaurantApi.getRestaurants()
                val sortedRestaurants = sortRestaurantsByDistance(restaurants)
                withContext(Dispatchers.Main) {
                    allRestaurants = sortedRestaurants
                    applyFilter()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), e.message ?: "Failed loading restaurants", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun sortRestaurantsByDistance(restaurants: List<Restaurant>): List<Restaurant> {
        val userLocation = LocationManager.userLocation
        if (!LocationManager.locationSortingEnabled || userLocation == null) {
            return restaurants
        }
        return restaurants
            .mapNotNull { restaurant ->
                val restaurantLocation = parseCoordinates(restaurant.location)
                if (restaurantLocation == null) {
                    null
                } else {
                    val distance = userLocation.distanceTo(restaurantLocation)
                    restaurant to distance
                }
            }
            .sortedBy { it.second }
            .map { it.first }
    }

    private fun parseCoordinates(location: String?): Location? {
        if (location.isNullOrBlank()) return null
        return try {
            val parts = location.split(",")
            if (parts.size != 2) return null
            val latitude = parts[0].trim().toDouble()
            val longitude = parts[1].trim().toDouble()
            if (latitude < -90 || latitude > 90) return null
            if (longitude < -180 || longitude > 180) return null
            Location("restaurant").apply {
                this.latitude = latitude
                this.longitude = longitude
            }
        } catch (e: Exception) {
            null
        }
    }
}