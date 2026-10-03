package com.example.tastee

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.tastee.dto.Dish
import com.example.tastee.dto.DishRequest
import com.example.tastee.network.RetrofitClient
import com.example.tastee.utils.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

class AddDishActivity : AppCompatActivity() {
    private var restaurantId: Long = -1L
    private var dishAvailability: Boolean = true
    private var dishId: Long = -1L
    private var isEditMode = false

    private var selectedImageUri: Uri? = null
    private var existingImagePath: String? = null

    private lateinit var nameInput: EditText
    private lateinit var imageInput: EditText
    private lateinit var priceInput: EditText
    private lateinit var categoryInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var prepTimeInput: EditText

    private lateinit var vegetarianCheckBox: CheckBox
    private lateinit var veganCheckBox: CheckBox
    private lateinit var spicyCheckBox: CheckBox

    private lateinit var saveDishButton: Button
    private lateinit var selectGalleryButton: ImageButton
    private lateinit var categoryDropdownButton: ImageButton
    private lateinit var dishImagePreview: ImageView

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            imageInput.setText("Selected from device")
            dishImagePreview.visibility = View.VISIBLE
            Glide.with(this)
                .load(it)
                .into(dishImagePreview)
            Toast.makeText(this, "Image selected from gallery", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_dish)

        restaurantId = intent.getLongExtra("RESTAURANT_ID", -1L)
        dishId = intent.getLongExtra("DISH_ID", -1L)
        isEditMode = dishId != -1L

        if (restaurantId == -1L) {
            Toast.makeText(this, "Invalid restaurant", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        nameInput = findViewById(R.id.nameInput)
        imageInput = findViewById(R.id.imageInput)
        priceInput = findViewById(R.id.priceInput)
        categoryInput = findViewById(R.id.categoryInput)
        descriptionInput = findViewById(R.id.descriptionInput)
        prepTimeInput = findViewById(R.id.prepTimeInput)

        vegetarianCheckBox = findViewById(R.id.vegetarianCheckBox)
        veganCheckBox = findViewById(R.id.veganCheckBox)
        spicyCheckBox = findViewById(R.id.spicyCheckBox)

        saveDishButton = findViewById(R.id.saveDishButton)
        selectGalleryButton = findViewById(R.id.selectGalleryButton)
        categoryDropdownButton = findViewById(R.id.categoryDropdownButton)
        dishImagePreview = findViewById(R.id.dishImagePreview)

        selectGalleryButton.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        categoryDropdownButton.setOnClickListener {
            showCategoryDropdown(it)
        }

        if (isEditMode) {
            saveDishButton.text = "Save Changes"
            loadDish()
        } else {
            saveDishButton.text = "Add Dish"
        }

        saveDishButton.setOnClickListener {
            saveDish()
        }
    }

    private fun showCategoryDropdown(anchor: View) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.dishApi.getAllDishes()
                val categories = response.mapNotNull { it.category }.distinct().sorted()

                val popup = PopupMenu(this@AddDishActivity, anchor)
                if (categories.isEmpty()) {
                    popup.menu.add("No existing categories")
                } else {
                    for (category in categories) {
                        popup.menu.add(category)
                    }
                }

                popup.setOnMenuItemClickListener { item ->
                    if (categories.contains(item.title.toString())) {
                        categoryInput.setText(item.title)
                        true
                    } else {
                        false
                    }
                }
                popup.show()
            } catch (e: Exception) {
                Toast.makeText(this@AddDishActivity, "Failed to load categories", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadDish() {
        lifecycleScope.launch {
            try {
                val dish = RetrofitClient.dishApi.getDish(dishId)
                populateFields(dish)
            } catch (e: Exception) {
                Toast.makeText(this@AddDishActivity, e.message ?: "Failed to load dish", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun populateFields(dish: Dish) {
        nameInput.setText(dish.name)

        var cleanPath = dish.image ?: ""
        if (cleanPath.startsWith("http://10.0.2.2:8080")) {
            cleanPath = cleanPath.removePrefix("http://10.0.2.2:8080")
        }
        existingImagePath = cleanPath

        if (cleanPath.isNotBlank()) {
            imageInput.setText(cleanPath)
        }

        val imageUrl = getSafeImageUrl(cleanPath)

        if (imageUrl.isNotBlank()) {
            dishImagePreview.visibility = View.VISIBLE
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(dishImagePreview)
        }

        priceInput.setText(dish.price.toString())
        categoryInput.setText(dish.category)
        descriptionInput.setText(dish.description ?: "")
        prepTimeInput.setText(dish.prepTime.toString())

        vegetarianCheckBox.isChecked = dish.vegetarian == true
        veganCheckBox.isChecked = dish.vegan == true
        dishAvailability = dish.availability
        spicyCheckBox.isChecked = dish.spicy == true
    }

    private fun getSafeImageUrl(path: String?): String {
        if (path.isNullOrBlank()) return ""

        if (path.startsWith("http://") || path.startsWith("https://") ||
            path.startsWith("content://") || path.startsWith("file://")) {

            if (path.contains("content://")) {
                val contentIndex = path.indexOf("content://")
                return path.substring(contentIndex)
            }
            return path
        }

        return "http://10.0.2.2:8080$path"
    }

    private fun saveDish() {
        val name = nameInput.text.toString().trim()
        val imageUrl = imageInput.text.toString().trim()
        val priceText = priceInput.text.toString().trim()
        val category = categoryInput.text.toString().trim()
        val description = descriptionInput.text.toString().trim()
        val prepTimeText = prepTimeInput.text.toString().trim()

        if (name.isBlank() || priceText.isBlank() || category.isBlank()) {
            Toast.makeText(this, "Fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val price = priceText.toFloatOrNull()
        val prepTime = prepTimeText.toIntOrNull()

        if (price == null || prepTime == null) {
            Toast.makeText(this, "Invalid price or preparation time", Toast.LENGTH_LONG).show()
            return
        }

        lifecycleScope.launch {
            try {
                var finalImagePath = if (imageUrl.isNotBlank()) imageUrl else existingImagePath

                if (selectedImageUri != null) {
                    val uploadedPath = uploadImageToServer(selectedImageUri!!)
                    if (uploadedPath != null) {
                        finalImagePath = uploadedPath
                    } else {
                        Toast.makeText(this@AddDishActivity, "Image upload failed", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                }

                if (isEditMode) {
                    val dish = Dish(
                        id = dishId,
                        name = name,
                        image = finalImagePath,
                        price = price,
                        category = category,
                        restaurantId = restaurantId,
                        description = description.ifBlank { null },
                        prepTime = prepTime,
                        vegetarian = vegetarianCheckBox.isChecked,
                        vegan = veganCheckBox.isChecked,
                        spicy = spicyCheckBox.isChecked,
                        discountPercentage = null,
                        availability = dishAvailability
                    )
                    val response = RetrofitClient.dishApi.updateDish(dishId, dish)
                    if (response.isSuccessful) {
                        Toast.makeText(this@AddDishActivity, "Food item updated!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@AddDishActivity, "Failed to update: ${response.code()}", Toast.LENGTH_LONG).show()
                    }
                } else {
                    val request = DishRequest(
                        name = name,
                        image = finalImagePath,
                        price = price,
                        category = category,
                        restaurantId = restaurantId,
                        description = description.ifBlank { null },
                        prepTime = prepTime,
                        vegetarian = vegetarianCheckBox.isChecked,
                        vegan = veganCheckBox.isChecked,
                        spicy = spicyCheckBox.isChecked,
                        discountPercentage = null
                    )
                    val response = RetrofitClient.dishApi.createDish(request)
                    if (response.isSuccessful) {
                        Toast.makeText(this@AddDishActivity, "Food item added!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@AddDishActivity, "Failed to create: ${response.code()}", Toast.LENGTH_LONG).show()
                    }
                }

            } catch (e: Exception) {
                Toast.makeText(this@AddDishActivity, e.message ?: "Failed to save dish", Toast.LENGTH_LONG).show()
            }
        }
    }

    private suspend fun uploadImageToServer(uri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                val file = ImageUtils.uriToFile(this@AddDishActivity, uri) ?: return@withContext null
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                val response = RetrofitClient.dishApi.uploadImage(body)

                if (response.isSuccessful) {
                    response.body()?.url
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}