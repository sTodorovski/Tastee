package com.example.tastee.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tastee.R
import com.example.tastee.dto.Dish

class DishAdapter(
    private var dishes: List<Dish>,
    private val onDishClicked: (Dish) -> Unit
) : RecyclerView.Adapter<DishAdapter.DishViewHolder>() {

    init {
        dishes = dishes.filter { it.availability }
    }

    class DishViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.dishImage)
        val name: TextView = itemView.findViewById(R.id.dishName)
        val prepTimeText: TextView = itemView.findViewById(R.id.dishPrepTime)
        val dietaryText: TextView = itemView.findViewById(R.id.dietaryIndicators)
        val originalPrice: TextView = itemView.findViewById(R.id.dishOriginalPrice)
        val discountedPrice: TextView = itemView.findViewById(R.id.dishPrice)
        val discountIndicator: TextView = itemView.findViewById(R.id.discountIndicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DishViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category_dish, parent, false)
        return DishViewHolder(view)
    }

    override fun onBindViewHolder(holder: DishViewHolder, position: Int) {
        val dish = dishes[position]

        holder.name.text = dish.name
        holder.prepTimeText.text = "⏱ ${dish.prepTime}m"

        val indicators = mutableListOf<String>()
        if (dish.spicy == true) indicators.add("🌶️")
        if (dish.vegan == true) indicators.add("🌱")
        if (dish.vegetarian == true && dish.vegan != true) indicators.add("🥗")

        if (indicators.isNotEmpty()) {
            holder.dietaryText.text = indicators.joinToString(" ")
            holder.dietaryText.visibility = View.VISIBLE
        } else {
            holder.dietaryText.visibility = View.GONE
        }

        val discount = dish.discountPercentage

        if (discount != null && discount > 0f) {
            val originalPrice = dish.price
            val discountedPrice = originalPrice * (1f - discount / 100f)

            holder.originalPrice.text = "$${"%.2f".format(originalPrice)}"
            holder.originalPrice.paintFlags = holder.originalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.originalPrice.visibility = View.VISIBLE

            holder.discountedPrice.text = "$${"%.2f".format(discountedPrice)}"

            val discountText = if (discount % 1f == 0f) {
                discount.toInt().toString()
            } else {
                "%.1f".format(discount)
            }

            holder.discountIndicator.text = "${discountText}% OFF"
            holder.discountIndicator.visibility = View.VISIBLE
        } else {
            holder.originalPrice.text = "$${"%.2f".format(dish.price)}"
            holder.originalPrice.paintFlags = holder.originalPrice.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.originalPrice.visibility = View.GONE

            holder.discountedPrice.text = "$${"%.2f".format(dish.price)}"
            holder.discountedPrice.visibility = View.VISIBLE
            holder.discountIndicator.visibility = View.GONE
        }

        val imageUrl = when {
            dish.image.isNullOrEmpty() -> null
            dish.image.startsWith("http") ||
                    dish.image.startsWith("content://") ||
                    dish.image.startsWith("file://") -> dish.image
            else -> "http://10.0.2.2:8080${dish.image}"
        }

        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .into(holder.image)

        holder.itemView.setOnClickListener {
            onDishClicked(dish)
        }
    }

    override fun getItemCount(): Int = dishes.size

    fun updateDishes(newDishes: List<Dish>) {
        dishes = newDishes.filter { it.availability }
        notifyDataSetChanged()
    }
}