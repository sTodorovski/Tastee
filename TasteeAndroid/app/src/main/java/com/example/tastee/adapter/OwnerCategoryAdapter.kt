package com.example.tastee.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tastee.R
import com.example.tastee.dto.Dish

data class OwnerCategorySection(
    val categoryName: String,
    val dishes: List<Dish>
)

class OwnerCategoryAdapter(
    private var categories: List<OwnerCategorySection>,
    private val onDishClicked: (Dish) -> Unit = {}
) : RecyclerView.Adapter<OwnerCategoryAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val categoryName: TextView =
            view.findViewById(R.id.categoryName)

        val recyclerView: RecyclerView =
            view.findViewById(R.id.categoryRecyclerView)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CategoryViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.item_category_section,
                parent,
                false
            )

        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: CategoryViewHolder,
        position: Int
    ) {

        val category = categories[position]

        holder.categoryName.text =
            category.categoryName

        holder.recyclerView.layoutManager =
            LinearLayoutManager(
                holder.itemView.context,
                LinearLayoutManager.HORIZONTAL,
                false
            )

        holder.recyclerView.adapter =
            OwnerDishAdapter(
                category.dishes,
                onDishClicked
            )
    }

    override fun getItemCount(): Int =
        categories.size

    fun updateCategories(
        newCategories: List<OwnerCategorySection>
    ) {

        categories = newCategories

        notifyDataSetChanged()
    }

}

private class OwnerDishAdapter(
    private val dishes: List<Dish>,
    private val onDishClicked: (Dish) -> Unit
) : RecyclerView.Adapter<OwnerDishAdapter.DishViewHolder>() {

    class DishViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val image: ImageView =
            view.findViewById(R.id.dishImage)

        val name: TextView =
            view.findViewById(R.id.dishName)

        val prepTimeText: TextView =
            view.findViewById(R.id.dishPrepTime)

        val dietaryText: TextView =
            view.findViewById(R.id.dietaryIndicators)

        val originalPrice: TextView =
            view.findViewById(R.id.dishOriginalPrice)

        val price: TextView =
            view.findViewById(R.id.dishPrice)

        val discountIndicator: TextView =
            view.findViewById(R.id.discountIndicator)

        val availability: TextView =
            view.findViewById(R.id.availabilityIndicator)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DishViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.item_category_dish,
                parent,
                false
            )

        return DishViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: DishViewHolder,
        position: Int
    ) {

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

        if (discount != null &&
            discount > 0f
        ) {

            val originalPrice = dish.price

            val discountedPrice =
                originalPrice *
                        (1f - discount / 100f)

            holder.originalPrice.text =
                String.format(
                    "$%.2f",
                    originalPrice
                )

            holder.originalPrice.visibility =
                View.VISIBLE

            holder.originalPrice.paintFlags =
                holder.originalPrice.paintFlags or
                        Paint.STRIKE_THRU_TEXT_FLAG

            holder.price.layoutParams =
                holder.price.layoutParams.apply {
                    (this as? android.view.ViewGroup.MarginLayoutParams)?.marginStart =
                        holder.itemView.context.resources.displayMetrics.density.times(12).toInt()
                }

            holder.price.text =
                String.format(
                    "$%.2f",
                    discountedPrice
                )

            val discountText =
                if (discount % 1f == 0f) {
                    discount.toInt().toString()
                } else {
                    String.format("%.1f", discount)
                }

            holder.discountIndicator.text =
                "${discountText}% OFF"

            holder.discountIndicator.visibility =
                View.VISIBLE

        } else {

            holder.originalPrice.visibility =
                View.GONE

            holder.price.layoutParams =
                holder.price.layoutParams.apply {
                    (this as? android.view.ViewGroup.MarginLayoutParams)?.marginStart = 0
                }

            holder.price.text =
                String.format(
                    "$%.2f",
                    dish.price
                )

            holder.discountIndicator.visibility =
                View.GONE
        }

        holder.availability.text =
            if (dish.availability) "✓"
            else "✕"

        val imageUrl = when {

            dish.image.isNullOrBlank() ->
                null

            dish.image.startsWith("http") ->
                dish.image

            else ->
                "http://10.0.2.2:8080${dish.image}"
        }

        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .placeholder(R.drawable.ic_person)
            .error(R.drawable.ic_person)
            .into(holder.image)

        holder.itemView.setOnClickListener {
            onDishClicked(dish)
        }
    }

    override fun getItemCount(): Int =
        dishes.size

}