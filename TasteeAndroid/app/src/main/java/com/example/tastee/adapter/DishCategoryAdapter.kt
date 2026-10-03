package com.example.tastee.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tastee.R
import com.example.tastee.dto.Dish

data class DishCategorySection(
    val categoryName: String,
    val dishes: List<Dish>
)

class DishCategoryAdapter(
    private var categories: List<DishCategorySection>,
    private val onDishClicked: (Dish) -> Unit
) : RecyclerView.Adapter<DishCategoryAdapter.CategoryViewHolder>() {

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
            holder.recyclerView.context,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        holder.recyclerView.adapter = DishAdapter(
            category.dishes,
            onDishClicked
        )
        holder.recyclerView.isNestedScrollingEnabled = false
        holder.recyclerView.setHasFixedSize(true)
        holder.recyclerView.clipToPadding = false
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    fun updateCategories(newCategories: List<DishCategorySection>) {
        categories = newCategories
        notifyDataSetChanged()
    }
}