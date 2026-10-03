package com.example.tastee.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tastee.R
import com.example.tastee.dto.Restaurant
import android.location.Location

class CategoryRestaurantAdapter(
    private var restaurants: List<CategoryRestaurant>,
    private var userLocation: Location? = null,
    private val onRestaurantClicked: (Restaurant) -> Unit
) : RecyclerView.Adapter<CategoryRestaurantAdapter.RestaurantViewHolder>() {

    class RestaurantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val distance: TextView = itemView.findViewById(R.id.restaurantDistance)
        val image: ImageView = itemView.findViewById(R.id.restaurantImage)
        val name: TextView = itemView.findViewById(R.id.restaurantName)
        val dishCount: TextView = itemView.findViewById(R.id.restaurantDishCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category_restaurant, parent, false)
        return RestaurantViewHolder(view)
    }

    override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int) {
        val categoryRestaurant = restaurants[position]
        val restaurant = categoryRestaurant.restaurant

        holder.name.text = restaurant.name
        holder.dishCount.text = "${categoryRestaurant.dishCount} dishes"

        val restaurantLocation = parseCoordinates(restaurant.location)

        if (userLocation != null && restaurantLocation != null) {
            val distanceMeters = userLocation!!.distanceTo(restaurantLocation)
            holder.distance.text = if (distanceMeters < 1000) {
                "${distanceMeters.toInt()} m away"
            } else {
                "%.1f km away".format(distanceMeters / 1000)
            }
            holder.distance.visibility = View.VISIBLE
        } else {
            holder.distance.visibility = View.GONE
        }

        val imageUrl = if (!restaurant.logo.isNullOrEmpty()) {
            if (restaurant.logo.startsWith("http")) {
                restaurant.logo
            } else if (restaurant.logo.startsWith("/")) {
                "http://10.0.2.2:8080${restaurant.logo}"
            } else {
                "http://10.0.2.2:8080/images/${restaurant.logo}"
            }
        } else {
            null
        }

        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .placeholder(R.drawable.ic_person)
            .error(R.drawable.ic_person)
            .into(holder.image)

        holder.itemView.setOnClickListener {
            onRestaurantClicked(restaurant)
        }
    }

    override fun getItemCount(): Int = restaurants.size

    fun updateRestaurants(newRestaurants: List<CategoryRestaurant>) {
        restaurants = newRestaurants
        notifyDataSetChanged()
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