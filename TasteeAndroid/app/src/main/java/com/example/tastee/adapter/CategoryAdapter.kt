package com.example.tastee.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tastee.R
import com.example.tastee.dto.Restaurant
import android.location.Location

class CategoryAdapter(
    private var categories: List<CategorySection>,
    private val onRestaurantClicked: (Restaurant) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private var userLocation: Location? = null

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryName: TextView = itemView.findViewById(R.id.categoryName)
        val recyclerView: RecyclerView = itemView.findViewById(R.id.categoryRecyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category_section, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.categoryName.text = category.categoryName
        holder.recyclerView.layoutManager = LinearLayoutManager(
            holder.itemView.context,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        holder.recyclerView.adapter = CategoryRestaurantAdapter(
            category.restaurants,
            userLocation,
            onRestaurantClicked
        )
    }

    override fun getItemCount(): Int = categories.size

    fun updateCategories(newCategories: List<CategorySection>, newUserLocation: Location? = userLocation) {
        categories = newCategories
        userLocation = newUserLocation
        notifyDataSetChanged()
    }
}