package com.example.tastee

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tastee.adapter.OwnerCategoryAdapter
import com.example.tastee.adapter.OwnerCategorySection
import com.example.tastee.dto.RestaurantStatusRequest
import com.example.tastee.network.RetrofitClient
import com.example.tastee.ui.OwnerDishBottomSheet
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.launch
import retrofit2.HttpException

class RestaurantOwnerActivity : AppCompatActivity() {

    private lateinit var categoryAdapter: OwnerCategoryAdapter

    private var restaurantId: Long = -1L
    private var restaurantOpen: Boolean = true

    private lateinit var profileButton: ShapeableImageView
    private lateinit var restaurantProfileButton: ShapeableImageView
    private lateinit var restaurantStatusButton: Button
    private lateinit var emptyStateTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_owner)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            val addDishButton = findViewById<MaterialButton>(R.id.addDishButton)
            val buttonParams = addDishButton.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
            buttonParams.bottomMargin = systemBars.bottom + 24
            addDishButton.layoutParams = buttonParams

            val recyclerView = findViewById<RecyclerView>(R.id.categoryRecyclerView)
            recyclerView.setPadding(0, 0, 0, systemBars.bottom + 100)

            insets
        }

        val restaurantName = findViewById<TextView>(R.id.restaurantName)
        val addDishButton = findViewById<MaterialButton>(R.id.addDishButton)
        val categoryRecyclerView = findViewById<RecyclerView>(R.id.categoryRecyclerView)

        profileButton = findViewById(R.id.profileButton)
        restaurantProfileButton = findViewById(R.id.restaurantProfileButton)
        restaurantStatusButton = findViewById(R.id.restaurantStatusButton)
        emptyStateTextView = findViewById(R.id.emptyStateTextView)

        categoryAdapter = OwnerCategoryAdapter(emptyList()) { dish ->
            OwnerDishBottomSheet(this, dish) {
                loadDishes()
            }.show()
        }

        categoryRecyclerView.layoutManager = LinearLayoutManager(this)
        categoryRecyclerView.adapter = categoryAdapter

        profileButton.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        restaurantProfileButton.setOnClickListener {
            startActivity(Intent(this, RestaurantProfileActivity::class.java))
        }

        restaurantStatusButton.setOnClickListener {
            showRestaurantStatusDialog()
        }

        addDishButton.setOnClickListener {
            if (restaurantId == -1L) {
                Toast.makeText(this, "Restaurant is still loading.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            startActivity(
                Intent(this, AddDishActivity::class.java).apply {
                    putExtra("RESTAURANT_ID", restaurantId)
                }
            )
        }

        loadProfilePicture()

        lifecycleScope.launch {
            try {
                val restaurant = RetrofitClient.restaurantApi.getMyRestaurant()

                restaurantId = restaurant.id
                restaurantOpen = restaurant.open ?: false
                restaurantName.text = "${restaurant.name} Menu"

                updateStatusButton()
                loadRestaurantLogo(restaurant.logo)
                loadDishes()
            } catch (e: Exception) {
                showError("Failed to load restaurant", e)
            }
        }
    }

    private fun showRestaurantStatusDialog() {
        val options = arrayOf("Open Restaurant", "Close Restaurant")
        val checkedItem = if (restaurantOpen) 0 else 1

        AlertDialog.Builder(this)
            .setTitle("Restaurant Status")
            .setSingleChoiceItems(options, checkedItem) { dialog, which ->
                val newStatus = which == 0
                if (newStatus != restaurantOpen) {
                    updateRestaurantStatus(newStatus)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateRestaurantStatus(open: Boolean) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.restaurantApi.updateMyRestaurantStatus(
                    RestaurantStatusRequest(open = open)
                )

                restaurantOpen = response.open ?: false
                updateStatusButton()

                val message = if (restaurantOpen) {
                    "Restaurant is now OPEN"
                } else {
                    "Restaurant is now CLOSED"
                }

                Toast.makeText(this@RestaurantOwnerActivity, message, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                showError("Failed to update restaurant status", e)
            }
        }
    }

    private fun updateStatusButton() {
        if (restaurantOpen) {
            restaurantStatusButton.text = "OPEN"
            restaurantStatusButton.backgroundTintList = android.content.res.ColorStateList.valueOf(
                getColor(android.R.color.holo_green_light)
            )
            restaurantStatusButton.setTextColor(getColor(android.R.color.black))
        } else {
            restaurantStatusButton.text = "CLOSED"
            restaurantStatusButton.backgroundTintList = android.content.res.ColorStateList.valueOf(
                getColor(android.R.color.holo_red_light)
            )
            restaurantStatusButton.setTextColor(getColor(android.R.color.white))
        }
    }

    private fun loadDishes() {
        if (restaurantId == -1L) return

        lifecycleScope.launch {
            try {
                val dishes = RetrofitClient.restaurantApi.getRestaurantDishes(restaurantId)

                if (dishes.isNullOrEmpty()) {
                    emptyStateTextView.visibility = View.VISIBLE
                } else {
                    emptyStateTextView.visibility = View.GONE
                }

                val categories = dishes
                    .groupBy { it.category.trim() }
                    .map { (category, categoryDishes) ->
                        OwnerCategorySection(
                            categoryName = category,
                            dishes = categoryDishes.sortedBy { it.name.lowercase() }
                        )
                    }
                    .sortedBy { it.categoryName.lowercase() }

                categoryAdapter.updateCategories(categories)
            } catch (e: Exception) {
                showError("Failed to load dishes", e)
            }
        }
    }

    private fun showError(operation: String, exception: Exception) {
        val message = when (exception) {
            is HttpException -> {
                val errorBody = exception.response()?.errorBody()?.string()
                "$operation\nHTTP ${exception.code()}\n$errorBody"
            }
            else -> {
                "$operation\n${exception.javaClass.simpleName}: ${exception.message}"
            }
        }

        android.util.Log.e("RestaurantOwnerActivity", message, exception)
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun loadProfilePicture() {
        lifecycleScope.launch {
            try {
                val user = RetrofitClient.userApi.getCurrentUser()

                if (!user.profilePicture.isNullOrBlank()) {
                    val imageUrl = if (user.profilePicture.startsWith("http")) {
                        user.profilePicture
                    } else {
                        "http://10.0.2.2:8080${user.profilePicture}"
                    }

                    Glide.with(this@RestaurantOwnerActivity)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .into(profileButton)
                } else {
                    profileButton.setImageResource(R.drawable.ic_person)
                }
            } catch (e: Exception) {
                profileButton.setImageResource(R.drawable.ic_person)
            }
        }
    }

    private fun loadRestaurantLogo(logo: String?) {
        if (logo.isNullOrBlank()) {
            restaurantProfileButton.setImageResource(R.drawable.ic_person)
            return
        }

        val imageUrl = when {
            logo.startsWith("http") -> logo
            logo.startsWith("/") -> "http://10.0.2.2:8080$logo"
            else -> "http://10.0.2.2:8080/$logo"
        }

        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.ic_person)
            .error(R.drawable.ic_person)
            .into(restaurantProfileButton)
    }

    override fun onResume() {
        super.onResume()

        if (::categoryAdapter.isInitialized && restaurantId != -1L) {
            loadDishes()
        }

        if (::profileButton.isInitialized) {
            loadProfilePicture()
        }

        if (::restaurantProfileButton.isInitialized) {
            loadRestaurant()
        }
    }

    private fun loadRestaurant() {
        lifecycleScope.launch {
            try {
                val restaurant = RetrofitClient.restaurantApi.getMyRestaurant()

                restaurantId = restaurant.id
                restaurantOpen = restaurant.open ?: false
                findViewById<TextView>(R.id.restaurantName).text = "${restaurant.name} Menu"

                updateStatusButton()
                loadRestaurantLogo(restaurant.logo)
            } catch (e: Exception) {
                android.util.Log.e("RestaurantOwnerActivity", "Failed to refresh restaurant", e)
            }
        }
    }
}