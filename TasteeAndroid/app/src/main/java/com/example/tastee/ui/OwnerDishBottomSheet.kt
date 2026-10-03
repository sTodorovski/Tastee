package com.example.tastee.ui

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.tastee.AddDishActivity
import com.example.tastee.R
import com.example.tastee.dto.Dish
import com.example.tastee.dto.DishAvailabilityRequest
import com.example.tastee.dto.DishDiscountRequest
import com.example.tastee.network.RetrofitClient
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OwnerDishBottomSheet(
    context: Context,
    private val dish: Dish,
    private val onDishChanged: () -> Unit = {}
) : BottomSheetDialog(context) {

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_owner_dish, null)
        setContentView(view)

        val image = view.findViewById<ImageView>(R.id.dishImage)
        val name = view.findViewById<TextView>(R.id.dishName)
        val category = view.findViewById<TextView>(R.id.dishCategory)
        val description = view.findViewById<TextView>(R.id.dishDescription)
        val stats = view.findViewById<TextView>(R.id.dishStats)
        val availableButton = view.findViewById<Button>(R.id.availableButton)
        val unavailableButton = view.findViewById<Button>(R.id.unavailableButton)
        val noDiscountButton = view.findViewById<Button>(R.id.noDiscountButton)
        val discount10Button = view.findViewById<Button>(R.id.discount10Button)
        val discount20Button = view.findViewById<Button>(R.id.discount20Button)
        val discount30Button = view.findViewById<Button>(R.id.discount30Button)
        val customDiscountInput = view.findViewById<EditText>(R.id.customDiscountInput)
        val saveDiscountButton = view.findViewById<Button>(R.id.saveDiscountButton)
        val editButton = view.findViewById<Button>(R.id.editDishButton)
        val deleteButton = view.findViewById<Button>(R.id.deleteDishButton)

        name.text = dish.name
        category.text = dish.category
        description.text = dish.description ?: "No description"
        stats.text = "${dish.prepTime} min • $${"%.2f".format(dish.price)}"

        val imageUrl = when {
            dish.image.isNullOrBlank() -> null
            dish.image.startsWith("http") -> dish.image
            else -> "http://10.0.2.2:8080${dish.image}"
        }

        Glide.with(context)
            .load(imageUrl)
            .placeholder(R.drawable.ic_person)
            .error(R.drawable.ic_person)
            .into(image)

        updateAvailabilityButtons(availableButton, unavailableButton, dish.availability)

        availableButton.setOnClickListener {
            if (!dish.availability) {
                updateAvailability(true, availableButton, unavailableButton)
            }
        }

        unavailableButton.setOnClickListener {
            if (dish.availability) {
                updateAvailability(false, availableButton, unavailableButton)
            }
        }

        var selectedDiscount: Float? = dish.discountPercentage
        updateDiscountButtons(noDiscountButton, discount10Button, discount20Button, discount30Button, selectedDiscount)

        if (selectedDiscount != null && selectedDiscount != 10f && selectedDiscount != 20f && selectedDiscount != 30f) {
            customDiscountInput.setText(
                if (selectedDiscount % 1f == 0f) {
                    selectedDiscount.toInt().toString()
                } else {
                    selectedDiscount.toString()
                }
            )
        }

        noDiscountButton.setOnClickListener {
            selectedDiscount = null
            customDiscountInput.text.clear()
            updateDiscountButtons(noDiscountButton, discount10Button, discount20Button, discount30Button, selectedDiscount)
        }

        discount10Button.setOnClickListener {
            selectedDiscount = 10f
            customDiscountInput.text.clear()
            updateDiscountButtons(noDiscountButton, discount10Button, discount20Button, discount30Button, selectedDiscount)
        }

        discount20Button.setOnClickListener {
            selectedDiscount = 20f
            customDiscountInput.text.clear()
            updateDiscountButtons(noDiscountButton, discount10Button, discount20Button, discount30Button, selectedDiscount)
        }

        discount30Button.setOnClickListener {
            selectedDiscount = 30f
            customDiscountInput.text.clear()
            updateDiscountButtons(noDiscountButton, discount10Button, discount20Button, discount30Button, selectedDiscount)
        }

        customDiscountInput.setOnClickListener {
            selectedDiscount = null
            updateDiscountButtons(noDiscountButton, discount10Button, discount20Button, discount30Button, selectedDiscount)
        }

        saveDiscountButton.setOnClickListener {
            val customText = customDiscountInput.text.toString().trim()
            val discount = if (customText.isNotEmpty()) {
                val value = customText.toFloatOrNull()
                if (value == null || value < 0f || value > 100f) {
                    Toast.makeText(context, "Enter a discount between 0 and 100%", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                value
            } else {
                selectedDiscount
            }
            updateDiscount(discount, saveDiscountButton)
        }

        editButton.setOnClickListener {
            val intent = Intent(context, AddDishActivity::class.java).apply {
                putExtra("DISH_ID", dish.id)
                putExtra("RESTAURANT_ID", dish.restaurantId)
            }
            context.startActivity(intent)
            dismiss()
        }

        deleteButton.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(context)
            .setTitle("Delete Dish")
            .setMessage("Are you sure you want to delete \"${dish.name}\"?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ -> deleteDish() }
            .show()
    }

    private fun deleteDish() {
        val deleteButton = findViewById<Button>(R.id.deleteDishButton)
        deleteButton?.isEnabled = false
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.dishApi.deleteDish(dish.id)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Dish deleted", Toast.LENGTH_SHORT).show()
                        dismiss()
                        onDishChanged()
                    } else {
                        Toast.makeText(context, "Failed to delete dish", Toast.LENGTH_SHORT).show()
                        deleteButton?.isEnabled = true
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to delete dish", Toast.LENGTH_SHORT).show()
                    deleteButton?.isEnabled = true
                }
            }
        }
    }

    private fun updateAvailability(
        availability: Boolean,
        availableButton: Button,
        unavailableButton: Button
    ) {
        availableButton.isEnabled = false
        unavailableButton.isEnabled = false
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.dishApi.updateAvailability(
                    dish.id,
                    DishAvailabilityRequest(availability)
                )
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        updateAvailabilityButtons(availableButton, unavailableButton, availability)
                        Toast.makeText(
                            context,
                            if (availability) "Dish is now available" else "Dish is now unavailable",
                            Toast.LENGTH_SHORT
                        ).show()
                        onDishChanged()
                        dismiss()
                    } else {
                        Toast.makeText(context, "Failed to update availability", Toast.LENGTH_SHORT).show()
                        availableButton.isEnabled = true
                        unavailableButton.isEnabled = true
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to update availability", Toast.LENGTH_SHORT).show()
                    availableButton.isEnabled = true
                    unavailableButton.isEnabled = true
                }
            }
        }
    }

    private fun updateDiscount(discount: Float?, saveButton: Button) {
        saveButton.isEnabled = false
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.dishApi.updateDiscount(
                    dish.id,
                    DishDiscountRequest(discount)
                )
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            context,
                            if (discount == null) "Discount removed" else "Discount set to ${discount}%",
                            Toast.LENGTH_SHORT
                        ).show()
                        onDishChanged()
                        dismiss()
                    } else {
                        Toast.makeText(context, "Failed to update discount", Toast.LENGTH_SHORT).show()
                        saveButton.isEnabled = true
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to update discount", Toast.LENGTH_SHORT).show()
                    saveButton.isEnabled = true
                }
            }
        }
    }

    private fun updateAvailabilityButtons(
        availableButton: Button,
        unavailableButton: Button,
        availability: Boolean
    ) {
        if (availability) {
            availableButton.setBackgroundColor(context.getColor(R.color.white))
            availableButton.setTextColor(context.getColor(R.color.black))
            unavailableButton.setBackgroundColor(context.getColor(R.color.black))
            unavailableButton.setTextColor(context.getColor(R.color.white))
        } else {
            availableButton.setBackgroundColor(context.getColor(R.color.black))
            availableButton.setTextColor(context.getColor(R.color.white))
            unavailableButton.setBackgroundColor(context.getColor(R.color.white))
            unavailableButton.setTextColor(context.getColor(R.color.black))
        }
    }

    private fun updateDiscountButtons(
        noDiscountButton: Button,
        discount10Button: Button,
        discount20Button: Button,
        discount30Button: Button,
        selectedDiscount: Float?
    ) {
        val buttons = listOf(
            noDiscountButton to null,
            discount10Button to 10f,
            discount20Button to 20f,
            discount30Button to 30f
        )
        buttons.forEach { (button, value) ->
            if (value == selectedDiscount) {
                button.setBackgroundColor(context.getColor(R.color.white))
                button.setTextColor(context.getColor(R.color.black))
            } else {
                button.setBackgroundColor(context.getColor(R.color.black))
                button.setTextColor(context.getColor(R.color.white))
            }
        }
    }
}