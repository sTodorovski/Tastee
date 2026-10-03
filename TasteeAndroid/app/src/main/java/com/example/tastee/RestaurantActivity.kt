package com.example.tastee

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tastee.adapter.DishCategoryAdapter
import com.example.tastee.adapter.DishCategorySection
import com.example.tastee.adapter.ReviewAdapter
import com.example.tastee.dto.CartManager
import com.example.tastee.dto.CreateReviewRequest
import com.example.tastee.network.RetrofitClient
import com.example.tastee.ui.DishBottomSheet
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.launch

class RestaurantActivity : AppCompatActivity() {

    private var restaurantId = -1L
    private var restaurantOpen = false

    private lateinit var restaurantCover: ImageView
    private lateinit var restaurantName: TextView
    private lateinit var restaurantDescription: TextView
    private lateinit var addReviewButton: FloatingActionButton

    private lateinit var profileButton: ShapeableImageView
    private lateinit var cartButton: ImageButton
    private lateinit var cartBadge: TextView

    private lateinit var dishRecyclerView: RecyclerView
    private lateinit var dishCategoryAdapter: DishCategoryAdapter

    private lateinit var reviewRecyclerView: RecyclerView
    private lateinit var reviewAdapter: ReviewAdapter

    private lateinit var menuTab: TextView
    private lateinit var reviewsTab: TextView
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant)

        restaurantId = intent.getLongExtra("restaurantId", -1L)

        if (restaurantId == -1L) {
            finish()
            return
        }

        restaurantCover = findViewById(R.id.restaurantCover)
        restaurantName = findViewById(R.id.restaurantName)
        restaurantDescription = findViewById(R.id.restaurantDescription)
        statusText = findViewById(R.id.statusText)
        addReviewButton = findViewById(R.id.addReviewButton)

        profileButton = findViewById(R.id.profileButton)
        cartButton = findViewById(R.id.cartButton)
        cartBadge = findViewById(R.id.cartBadge)

        dishRecyclerView = findViewById(R.id.dishRecyclerView)
        reviewRecyclerView = findViewById(R.id.reviewRecyclerView)

        menuTab = findViewById(R.id.menuTab)
        reviewsTab = findViewById(R.id.reviewsTab)

        loadProfilePicture()

        dishCategoryAdapter = DishCategoryAdapter(emptyList()) { dish ->
            if (!restaurantOpen) {
                Toast.makeText(
                    this,
                    "This restaurant is currently not accepting orders.",
                    Toast.LENGTH_SHORT
                ).show()
                return@DishCategoryAdapter
            }

            DishBottomSheet(this, dish) {
                updateCartBadge()
            }.show()
        }

        dishRecyclerView.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )

        dishRecyclerView.adapter = dishCategoryAdapter

        reviewAdapter = ReviewAdapter(emptyList())

        reviewRecyclerView.layoutManager = LinearLayoutManager(this)
        reviewRecyclerView.adapter = reviewAdapter

        cartButton.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }

        menuTab.setOnClickListener {
            showMenu()
        }

        reviewsTab.setOnClickListener {
            showReviews()
        }

        addReviewButton.setOnClickListener {
            showAddReviewDialog()
        }

        loadRestaurant()
        loadDishes()
        loadReviews()
        updateCartBadge()
    }

    private fun showMenu() {
        dishRecyclerView.visibility = RecyclerView.VISIBLE
        reviewRecyclerView.visibility = RecyclerView.GONE

        menuTab.setTextColor(getColor(android.R.color.white))
        reviewsTab.setTextColor(getColor(android.R.color.darker_gray))

        addReviewButton.visibility = View.GONE
    }

    private fun showReviews() {
        dishRecyclerView.visibility = RecyclerView.GONE
        reviewRecyclerView.visibility = RecyclerView.VISIBLE

        menuTab.setTextColor(getColor(android.R.color.darker_gray))
        reviewsTab.setTextColor(getColor(android.R.color.white))

        addReviewButton.visibility = View.VISIBLE
    }

    private fun getImageUrl(image: String?): String {
        if (image.isNullOrBlank()) {
            return ""
        }

        return if (image.startsWith("http")) {
            image
        } else {
            "http://10.0.2.2:8080$image"
        }
    }

    private fun loadRestaurant() {
        lifecycleScope.launch {
            try {
                val restaurant = RetrofitClient.restaurantApi.getRestaurant(restaurantId)

                restaurantName.text = restaurant.name
                restaurantDescription.text = restaurant.description

                Glide.with(this@RestaurantActivity)
                    .load(getImageUrl(restaurant.cover))
                    .into(restaurantCover)

                loadOrderStatus()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadOrderStatus() {
        lifecycleScope.launch {
            try {
                val status = RetrofitClient.restaurantApi.getRestaurantOrderStatus(restaurantId)

                restaurantOpen = status.acceptingOrders

                if (restaurantOpen) {
                    statusText.text = "🟢 OPEN"
                    statusText.setTextColor(
                        android.graphics.Color.rgb(76, 175, 80)
                    )
                } else {
                    statusText.text = "🔴 CLOSED"
                    statusText.setTextColor(
                        android.graphics.Color.rgb(244, 67, 54)
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()

                restaurantOpen = false
                statusText.text = "🔴 CLOSED"
                statusText.setTextColor(
                    android.graphics.Color.rgb(244, 67, 54)
                )
            }
        }
    }

    private fun loadDishes() {
        lifecycleScope.launch {
            try {
                val dishes = RetrofitClient.restaurantApi.getRestaurantDishes(restaurantId)

                val groupedCategories = dishes
                    .filter { it.availability }
                    .groupBy {
                        it.category
                            ?.takeIf { category -> category.isNotBlank() }
                            ?: "Other"
                    }
                    .map { (categoryName, categoryDishes) ->
                        DishCategorySection(
                            categoryName = categoryName,
                            dishes = categoryDishes
                        )
                    }

                dishCategoryAdapter.updateCategories(groupedCategories)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadReviews() {
        lifecycleScope.launch {
            try {
                val reviews = RetrofitClient.restaurantApi.getRestaurantReviews(restaurantId)
                reviewAdapter.updateReviews(reviews)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateCartBadge() {
        val count = CartManager.totalItems()

        if (count == 0) {
            cartBadge.visibility = TextView.GONE
        } else {
            cartBadge.visibility = TextView.VISIBLE
            cartBadge.text = count.toString()
        }
    }

    override fun onResume() {
        super.onResume()
        updateCartBadge()
        loadProfilePicture()
        loadRestaurant()
    }

    private fun showAddReviewDialog() {
        val dialogView = layoutInflater.inflate(
            R.layout.dialog_add_review,
            null
        )

        val ratingBar = dialogView.findViewById<RatingBar>(R.id.reviewRatingBar)
        val commentEditText = dialogView.findViewById<EditText>(R.id.reviewCommentEditText)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Submit", null)
            .create()

        dialog.setOnShowListener {
            dialog.window?.setBackgroundDrawableResource(R.color.darkgrey)

            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(getColor(R.color.white))

            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(getColor(R.color.white))

            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {
                    val rating = ratingBar.rating.toInt()
                    val comment = commentEditText.text.toString().trim()

                    if (rating == 0) {
                        ratingBar.requestFocus()
                        return@setOnClickListener
                    }

                    submitReview(rating, comment, dialog)
                }
        }

        dialog.show()
    }

    private fun submitReview(
        rating: Int,
        comment: String,
        dialog: AlertDialog
    ) {
        lifecycleScope.launch {
            try {
                val request = CreateReviewRequest(
                    rating = rating,
                    comment = comment.ifBlank { null }
                )

                RetrofitClient.restaurantApi.addReview(restaurantId, request)

                dialog.dismiss()
                loadReviews()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadProfilePicture() {
        lifecycleScope.launch {
            try {
                val user = RetrofitClient.userApi.getCurrentUser()

                if (!user.profilePicture.isNullOrBlank()) {
                    val imageUrl = getImageUrl(user.profilePicture)

                    Glide.with(this@RestaurantActivity)
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
}