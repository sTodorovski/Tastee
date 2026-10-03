package com.example.tastee.ui

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.tastee.R
import com.example.tastee.dto.CartItem
import com.example.tastee.dto.CartManager
import com.example.tastee.dto.Dish
import com.google.android.material.bottomsheet.BottomSheetDialog

class DishBottomSheet(
    context: Context,
    private val dish: Dish,
    private val onItemAdded: () -> Unit
) : BottomSheetDialog(context) {

    private var quantity = 1

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_dish, null)
        setContentView(view)

        val image = view.findViewById<ImageView>(R.id.dishImage)
        val name = view.findViewById<TextView>(R.id.dishName)
        val category = view.findViewById<TextView>(R.id.dishCategory)
        val description = view.findViewById<TextView>(R.id.dishDescription)
        val prepTimeText = view.findViewById<TextView>(R.id.dishPrepTime)
        val dietaryText = view.findViewById<TextView>(R.id.dietaryIndicators)
        val stats = view.findViewById<TextView>(R.id.dishStats)
        val minus = view.findViewById<TextView>(R.id.minusButton)
        val plus = view.findViewById<TextView>(R.id.plusButton)
        val quantityText = view.findViewById<TextView>(R.id.quantityText)
        val add = view.findViewById<Button>(R.id.addToCartButton)

        name.text = dish.name
        category.text = dish.category
        description.text = dish.description ?: "No description"
        prepTimeText.text = "⏱ ${dish.prepTime} min"

        val indicators = mutableListOf<String>()
        if (dish.spicy == true) indicators.add("🌶")
        if (dish.vegan == true) indicators.add("🌱")
        if (dish.vegetarian == true && dish.vegan != true) indicators.add("🥗")

        if (indicators.isNotEmpty()) {
            dietaryText.text = indicators.joinToString(" ")
            dietaryText.visibility = View.VISIBLE
        } else {
            dietaryText.visibility = View.GONE
        }

        updateDishStats(stats)

        val imageUrl = if (dish.image?.startsWith("http") == true) {
            dish.image
        } else {
            "http://10.0.2.2:8080${dish.image}"
        }

        Glide.with(context).load(imageUrl).into(image)

        quantityText.text = quantity.toString()
        updateAddToCartButton(add)

        minus.setOnClickListener {
            if (quantity > 1) {
                quantity--
                quantityText.text = quantity.toString()
                updateAddToCartButton(add)
            }
        }

        plus.setOnClickListener {
            quantity++
            quantityText.text = quantity.toString()
            updateAddToCartButton(add)
        }

        add.setOnClickListener {
            println("ADDING TO CART")
            println("Dish ID: ${dish.id}")
            println("Dish name: ${dish.name}")
            println("Dish restaurant ID: ${dish.restaurantId}")
            println("Quantity: $quantity")

            val restaurantId = dish.restaurantId
            if (restaurantId == null) {
                Toast.makeText(context, "Unable to add this dish to the cart.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val unitPrice = getDiscountedPrice()
            val cartItem = CartItem(
                dishId = dish.id,
                restaurantId = restaurantId,
                name = dish.name,
                price = unitPrice,
                image = dish.image,
                quantity = quantity
            )

            println("CART ITEM: $cartItem")
            val added = CartManager.addItem(cartItem)
            println("CART ADD RESULT: $added")

            if (!added) {
                Toast.makeText(context, "You can only order from one restaurant at a time.", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Added $quantity ${dish.name} to cart", Toast.LENGTH_SHORT).show()
                onItemAdded()
                dismiss()
            }
        }
    }

    private fun getDiscountedPrice(): Float {
        val discount = dish.discountPercentage
        return if (discount != null && discount > 0f) {
            dish.price * (1f - discount / 100f)
        } else {
            dish.price
        }
    }

    private fun updateDishStats(stats: TextView) {
        val discount = dish.discountPercentage
        if (discount != null && discount > 0f) {
            val originalPrice = String.format("$%.2f", dish.price)
            val discountedPrice = String.format("$%.2f", getDiscountedPrice())
            val priceText = SpannableString("$originalPrice  $discountedPrice")
            priceText.setSpan(StrikethroughSpan(), 0, originalPrice.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            stats.text = priceText

            val discountText = if (discount % 1f == 0f) {
                discount.toInt().toString()
            } else {
                String.format("%.1f", discount)
            }
            stats.append("  ${discountText}% OFF")
        } else {
            stats.text = "$${"%.2f".format(dish.price)}"
        }
    }

    private fun updateAddToCartButton(button: Button) {
        val totalPrice = getDiscountedPrice() * quantity
        button.text = "Add to Cart\n$${"%.2f".format(totalPrice)}"
    }
}