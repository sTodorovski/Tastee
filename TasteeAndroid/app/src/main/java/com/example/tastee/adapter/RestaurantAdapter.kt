package com.example.tastee.adapter

import android.location.Location
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tastee.R
import com.example.tastee.dto.Restaurant
import java.util.Locale

class RestaurantAdapter(
    private var restaurants: List<Restaurant>,
    private var userLatitude: Double? = null,
    private var userLongitude: Double? = null,
    private val onRestaurantClick: (Restaurant) -> Unit
) : RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder>() {

    class RestaurantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.nameInput)
        val location: TextView = itemView.findViewById(R.id.locationInput)
        val description: TextView = itemView.findViewById(R.id.descriptionInput)
        val rating: TextView = itemView.findViewById(R.id.ratingText)
        val logo: ImageView = itemView.findViewById(R.id.restaurantLogo)
        val distance: TextView = itemView.findViewById(R.id.distanceText)
        val status: TextView = itemView.findViewById(R.id.statusText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_restaurant, parent, false)
        return RestaurantViewHolder(view)
    }

    override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int) {
        val restaurant = restaurants[position]
        holder.name.text = restaurant.name
        holder.location.text = restaurant.location
        holder.description.text = restaurant.description
        val avgRating = 0.0
        val reviewCount = 0
        if (reviewCount > 0) {
            holder.rating.text = String.format(Locale.US, "⭐ %.1f (%d)", avgRating, reviewCount)
        } else {
            holder.rating.text = "⭐ New"
        }
        if (restaurant.open == true) {
            holder.status.text = "🟢 OPEN"
            holder.status.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_green_light))
        } else {
            holder.status.text = "🔴 CLOSED"
            holder.status.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_red_light))
        }
        if (userLatitude != null && userLongitude != null) {
            val coordinates = parseCoordinates(restaurant.location)
            if (coordinates != null) {
                val results = FloatArray(1)
                Location.distanceBetween(userLatitude!!, userLongitude!!, coordinates.first, coordinates.second, results)
                val distanceMeters = results[0]
                holder.distance.text = if (distanceMeters < 1000) {
                    "${distanceMeters.toInt()} m away"
                } else {
                    "%.1f km away".format(distanceMeters / 1000)
                }
                holder.distance.visibility = View.VISIBLE
            } else {
                holder.distance.visibility = View.GONE
            }
        } else {
            holder.distance.visibility = View.GONE
        }
        val logoUrl = restaurant.logo?.let {
            if (it.startsWith("/")) {
                "http://10.0.2.2:8080$it"
            } else {
                "http://10.0.2.2:8080/images/$it"
            }
        }
        Glide.with(holder.itemView.context).load(logoUrl).into(holder.logo)
        holder.itemView.setOnClickListener {
            onRestaurantClick(restaurant)
        }
    }

    override fun getItemCount(): Int = restaurants.size

    fun updateRestaurants(newRestaurants: List<Restaurant>) {
        restaurants = newRestaurants
        notifyDataSetChanged()
    }

    private fun parseCoordinates(location: String?): Pair<Double, Double>? {
        if (location.isNullOrBlank()) return null
        return try {
            val parts = location.split(",")
            if (parts.size != 2) return null
            val latitude = parts[0].trim().toDouble()
            val longitude = parts[1].trim().toDouble()
            if (latitude !in -90.0..90.0 || longitude !in -180.0..180.0) {
                return null
            }
            Pair(latitude, longitude)
        } catch (e: Exception) {
            null
        }
    }

    fun updateUserLocation(latitude: Double, longitude: Double) {
        userLatitude = latitude
        userLongitude = longitude
        notifyDataSetChanged()
    }
}