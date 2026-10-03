package com.example.tastee

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.tastee.dto.Restaurant
import com.example.tastee.dto.RestaurantUpdateRequest
import com.example.tastee.dto.RestaurantWorkingHour
import com.example.tastee.dto.RestaurantWorkingHourRequest
import com.example.tastee.network.RetrofitClient
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.time.DayOfWeek
import java.util.Locale

class RestaurantProfileActivity : AppCompatActivity() {

    private lateinit var restaurantCoverImage: ShapeableImageView
    private lateinit var changeCoverButton: Button
    private lateinit var restaurantLogoImage: ShapeableImageView
    private lateinit var changeLogoButton: Button

    private lateinit var restaurantRatingText: TextView
    private lateinit var restaurantNameInput: EditText
    private lateinit var restaurantEmailInput: EditText
    private lateinit var restaurantPhoneInput: EditText
    private lateinit var restaurantLocationInput: EditText
    private lateinit var restaurantDescriptionInput: EditText

    private lateinit var workingHoursContainer: LinearLayout
    private lateinit var saveRestaurantButton: Button
    private lateinit var btnOrderHistory: ImageButton

    private val daySchedules = mutableMapOf<String, DaySchedule>()
    private var existingWorkingHours = ""

    private var currentCoverUrl: String? = null
    private var currentLogoUrl: String? = null

    private val days = listOf(
        "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"
    )

    data class DaySchedule(
        var open: Boolean,
        var openTime: String?,
        var closeTime: String?,
        var openButton: Button,
        var closeButton: Button,
        var switch: SwitchMaterial
    )

    private val coverPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                Glide.with(this).load(uri).into(restaurantCoverImage)
                uploadPicture(uri, isLogo = false)
            }
        }
    }

    private val logoPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                Glide.with(this).load(uri).into(restaurantLogoImage)
                uploadPicture(uri, isLogo = true)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_profile)

        restaurantCoverImage = findViewById(R.id.restaurantCoverImage)
        changeCoverButton = findViewById(R.id.changeCoverButton)
        restaurantLogoImage = findViewById(R.id.restaurantLogoImage)
        changeLogoButton = findViewById(R.id.changeLogoButton)

        restaurantRatingText = findViewById(R.id.restaurantRatingText)
        restaurantNameInput = findViewById(R.id.restaurantNameInput)
        restaurantEmailInput = findViewById(R.id.restaurantEmailInput)
        restaurantPhoneInput = findViewById(R.id.restaurantPhoneInput)
        restaurantLocationInput = findViewById(R.id.restaurantLocationInput)
        restaurantDescriptionInput = findViewById(R.id.restaurantDescriptionInput)

        workingHoursContainer = findViewById(R.id.workingHoursContainer)
        saveRestaurantButton = findViewById(R.id.saveRestaurantButton)
        btnOrderHistory = findViewById(R.id.btnOrderHistory)

        btnOrderHistory.setOnClickListener {
            val intent = Intent(this, OrderHistoryActivity::class.java)
            startActivity(intent)
        }

        changeCoverButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            coverPickerLauncher.launch(intent)
        }

        changeLogoButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            logoPickerLauncher.launch(intent)
        }

        createWorkingHoursRows()
        loadRestaurant()

        saveRestaurantButton.setOnClickListener {
            saveRestaurant()
        }
    }

    private fun createWorkingHoursRows() {
        workingHoursContainer.removeAllViews()
        daySchedules.clear()

        for (day in days) {
            val isWeekend = day == "SATURDAY" || day == "SUNDAY"
            val defaultIsOpen = !isWeekend
            val defaultOpenTime = "09:00"
            val defaultCloseTime = "17:00"

            val card = MaterialCardView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 12.dpToPx())
                }
                setCardBackgroundColor(Color.parseColor("#1A1A1A"))
                strokeColor = Color.parseColor("#333333")
                strokeWidth = 1.dpToPx()
                radius = 12f.dpToPx().toFloat()
                useCompatPadding = false
            }

            val cardContent = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16.dpToPx(), 12.dpToPx(), 16.dpToPx(), 12.dpToPx())
            }

            val header = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            val dayText = TextView(this).apply {
                text = formatDayName(day)
                textSize = 16f
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(getColor(R.color.white))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val daySwitch = SwitchMaterial(this).apply {
                text = if (defaultIsOpen) "Open" else "Closed"
                isChecked = defaultIsOpen
                setTextColor(Color.parseColor("#AAAAAA"))
            }

            header.addView(dayText)
            header.addView(daySwitch)

            val timesContainer = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 8.dpToPx()
                }
            }

            val openButton = MaterialButton(
                this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle
            ).apply {
                text = "Opens: $defaultOpenTime"
                setTextColor(getColor(R.color.white))
                strokeColor = ColorStateList.valueOf(Color.parseColor("#444444"))
                cornerRadius = 8.dpToPx()
                isEnabled = defaultIsOpen
                alpha = if (defaultIsOpen) 1.0f else 0.4f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginEnd = 6.dpToPx()
                }
            }

            val closeButton = MaterialButton(
                this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle
            ).apply {
                text = "Closes: $defaultCloseTime"
                setTextColor(getColor(R.color.white))
                strokeColor = ColorStateList.valueOf(Color.parseColor("#444444"))
                cornerRadius = 8.dpToPx()
                isEnabled = defaultIsOpen
                alpha = if (defaultIsOpen) 1.0f else 0.4f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginStart = 6.dpToPx()
                }
            }

            timesContainer.addView(openButton)
            timesContainer.addView(closeButton)

            cardContent.addView(header)
            cardContent.addView(timesContainer)
            card.addView(cardContent)

            workingHoursContainer.addView(card)

            val schedule = DaySchedule(
                open = defaultIsOpen,
                openTime = defaultOpenTime,
                closeTime = defaultCloseTime,
                openButton = openButton,
                closeButton = closeButton,
                switch = daySwitch
            )

            daySchedules[day] = schedule

            daySwitch.setOnCheckedChangeListener { _, isChecked ->
                schedule.open = isChecked
                openButton.isEnabled = isChecked
                closeButton.isEnabled = isChecked
                openButton.alpha = if (isChecked) 1.0f else 0.4f
                closeButton.alpha = if (isChecked) 1.0f else 0.4f
                daySwitch.text = if (isChecked) "Open" else "Closed"
            }

            openButton.setOnClickListener { showTimePicker(schedule, true) }
            closeButton.setOnClickListener { showTimePicker(schedule, false) }
        }
    }

    private fun showTimePicker(schedule: DaySchedule, isOpeningTime: Boolean) {
        val currentTime = if (isOpeningTime) schedule.openTime ?: "09:00" else schedule.closeTime ?: "17:00"
        val parts = currentTime.split(":")
        val currentHour = parts.getOrNull(0)?.toIntOrNull() ?: 9
        val currentMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0

        TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                val selectedTime = String.format(Locale.US, "%02d:%02d", hourOfDay, minute)
                if (isOpeningTime) {
                    schedule.openTime = selectedTime
                    schedule.openButton.text = "Opens: $selectedTime"
                } else {
                    schedule.closeTime = selectedTime
                    schedule.closeButton.text = "Closes: $selectedTime"
                }
            },
            currentHour,
            currentMinute,
            true
        ).show()
    }

    private fun loadRestaurant() {
        lifecycleScope.launch {
            try {
                val restaurant = RetrofitClient.restaurantApi.getMyRestaurant()
                populateFields(restaurant)
                existingWorkingHours = restaurant.workingHours ?: ""

                loadWorkingHours()
                loadRating(restaurant.id)
            } catch (e: Exception) {
                Toast.makeText(
                    this@RestaurantProfileActivity,
                    "Failed to load restaurant settings",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun loadRating(restaurantId: Long) {
        lifecycleScope.launch {
            try {
                val reviews = RetrofitClient.restaurantApi.getRestaurantReviews(restaurantId)
                if (reviews.isNotEmpty()) {
                    val avgRating = reviews.map { it.rating }.average()
                    val totalReviews = reviews.size
                    restaurantRatingText.text = "⭐ ${String.format(Locale.US, "%.1f", avgRating)} / 5 ($totalReviews reviews)"
                } else {
                    restaurantRatingText.text = "⭐ No reviews yet"
                }
            } catch (e: Exception) {
                restaurantRatingText.text = "⭐ Rating unavailable"
            }
        }
    }

    private fun populateFields(restaurant: Restaurant) {
        restaurantNameInput.setText(restaurant.name)
        restaurantEmailInput.setText(restaurant.email)
        restaurantPhoneInput.setText(restaurant.phoneNumber)
        restaurantLocationInput.setText(restaurant.location)
        restaurantDescriptionInput.setText(restaurant.description)

        currentLogoUrl = restaurant.logo
        currentCoverUrl = restaurant.cover

        loadPreview(currentLogoUrl, restaurantLogoImage)
        loadPreview(currentCoverUrl, restaurantCoverImage)
    }

    private suspend fun loadWorkingHours() {
        try {
            val schedule = RetrofitClient.restaurantApi.getMyWorkingHours()
            if (schedule.size == 7) {
                applyWorkingHours(schedule)
            }
        } catch (e: Exception) {
        }
    }

    private fun applyWorkingHours(schedule: List<RestaurantWorkingHour>) {
        for (workingHour in schedule) {
            val day = workingHour.dayOfWeek.toString().uppercase(Locale.ROOT)
            val daySchedule = daySchedules[day] ?: continue
            val isOpen = workingHour.closed != true

            daySchedule.open = isOpen
            daySchedule.switch.isChecked = isOpen
            daySchedule.switch.text = if (isOpen) "Open" else "Closed"

            daySchedule.openTime = formatTime(workingHour.openTime)
            daySchedule.closeTime = formatTime(workingHour.closeTime)

            daySchedule.openButton.text = "Opens: ${daySchedule.openTime ?: "09:00"}"
            daySchedule.closeButton.text = "Closes: ${daySchedule.closeTime ?: "17:00"}"

            daySchedule.openButton.isEnabled = isOpen
            daySchedule.closeButton.isEnabled = isOpen
            daySchedule.openButton.alpha = if (isOpen) 1.0f else 0.4f
            daySchedule.closeButton.alpha = if (isOpen) 1.0f else 0.4f
        }
    }

    private fun formatTime(time: String?): String? {
        if (time.isNullOrBlank()) return null
        val parts = time.split(":")
        if (parts.size < 2) return time

        return String.format(
            Locale.US,
            "%02d:%02d",
            parts[0].toIntOrNull() ?: return time,
            parts[1].toIntOrNull() ?: return time
        )
    }

    private fun uploadPicture(uri: Uri, isLogo: Boolean) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val fileName = if (isLogo) "restaurant_logo.jpg" else "restaurant_cover.jpg"
                val file = File(cacheDir, fileName)

                inputStream?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                val requestFile = file.asRequestBody("image/*".toMediaType())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                val response = RetrofitClient.restaurantApi.uploadImage(body)
                val uploadedPath = response.url.trim()

                withContext(Dispatchers.Main) {
                    if (isFinishing || isDestroyed) return@withContext
                    if (isLogo) {
                        currentLogoUrl = uploadedPath
                        loadPreview(uploadedPath, restaurantLogoImage)
                        Toast.makeText(this@RestaurantProfileActivity, "Logo image uploaded", Toast.LENGTH_SHORT).show()
                    } else {
                        currentCoverUrl = uploadedPath
                        loadPreview(uploadedPath, restaurantCoverImage)
                        Toast.makeText(this@RestaurantProfileActivity, "Cover image uploaded", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (isFinishing || isDestroyed) return@withContext
                    val imageType = if (isLogo) "logo" else "cover image"
                    Toast.makeText(this@RestaurantProfileActivity, "Failed to upload $imageType: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun saveRestaurant() {
        if (!validateWorkingHours()) return

        val request = RestaurantUpdateRequest(
            name = restaurantNameInput.text.toString().trim(),
            email = restaurantEmailInput.text.toString().trim(),
            phoneNumber = restaurantPhoneInput.text.toString().trim(),
            location = restaurantLocationInput.text.toString().trim(),
            workingHours = existingWorkingHours,
            description = restaurantDescriptionInput.text.toString().trim(),
            logo = currentLogoUrl,
            cover = currentCoverUrl
        )

        val workingHoursRequests = days.map { day ->
            val schedule = daySchedules[day] ?: throw IllegalStateException("Missing schedule for $day")
            RestaurantWorkingHourRequest(
                dayOfWeek = DayOfWeek.valueOf(day),
                openTime = if (schedule.open) schedule.openTime else null,
                closeTime = if (schedule.open) schedule.closeTime else null,
                closed = !schedule.open
            )
        }

        lifecycleScope.launch {
            try {
                RetrofitClient.restaurantApi.updateMyRestaurant(request)
                RetrofitClient.restaurantApi.updateMyWorkingHours(workingHoursRequests)

                existingWorkingHours = request.workingHours
                loadPreview(request.cover, restaurantCoverImage)
                loadPreview(request.logo, restaurantLogoImage)

                Toast.makeText(
                    this@RestaurantProfileActivity,
                    "Restaurant settings saved",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    this@RestaurantProfileActivity,
                    e.message ?: "Failed to save restaurant settings",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun validateWorkingHours(): Boolean {
        for (day in days) {
            val schedule = daySchedules[day] ?: continue
            if (!schedule.open) continue

            val openTime = schedule.openTime
            val closeTime = schedule.closeTime

            if (openTime.isNullOrBlank() || closeTime.isNullOrBlank()) {
                Toast.makeText(this, "${formatDayName(day)} needs an opening and closing time.", Toast.LENGTH_LONG).show()
                return false
            }

            if (openTime >= closeTime) {
                Toast.makeText(this, "Closing time must be after opening time on ${formatDayName(day)}.", Toast.LENGTH_LONG).show()
                return false
            }
        }
        return true
    }

    private fun formatDayName(day: String): String {
        return day.lowercase(Locale.ROOT).replaceFirstChar { it.uppercase(Locale.ROOT) }
    }

    private fun loadPreview(imagePath: String?, targetView: ShapeableImageView) {
        if (isFinishing || isDestroyed) return

        if (imagePath.isNullOrBlank()) {
            targetView.setImageResource(R.drawable.ic_person)
            return
        }

        val cleanedPath = imagePath.replace("\"", "").trim()

        val imageModel: Any = when {
            cleanedPath.startsWith("content://") || cleanedPath.startsWith("file://") -> Uri.parse(cleanedPath)
            cleanedPath.startsWith("http://") || cleanedPath.startsWith("https://") -> cleanedPath
            cleanedPath.startsWith("/") -> "http://10.0.2.2:8080$cleanedPath"
            else -> "http://10.0.2.2:8080/$cleanedPath"
        }

        Glide.with(this)
            .load(imageModel)
            .placeholder(R.drawable.ic_person)
            .error(R.drawable.ic_person)
            .into(targetView)
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
    private fun Float.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}