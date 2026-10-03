package com.example.tastee

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.example.tastee.dto.RestaurantRequest
import com.example.tastee.dto.RestaurantWorkingHourRequest
import com.example.tastee.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.time.DayOfWeek
import java.util.Calendar
import java.util.Locale

class RegisterBusinessActivity : AppCompatActivity() {

    private var selectedLogoUri: Uri? = null
    private var selectedCoverUri: Uri? = null

    private val draftPrefsName = "restaurant_registration_draft"
    private var isRestoring = false

    private val logoPickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedLogoUri = it
            findViewById<EditText>(R.id.logoInput).setText("Logo selected")
            saveDraft()
        }
    }

    private val coverPickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedCoverUri = it
            findViewById<EditText>(R.id.coverInput).setText("Cover selected")
            saveDraft()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.black)
        setContentView(R.layout.activity_register_business)

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    saveDraft()
                    navigateToLogin()
                }
            }
        )

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.scrollView)
        ) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val root = findViewById<ConstraintLayout>(R.id.main)
        val nameInput = findViewById<EditText>(R.id.nameInput)
        val emailInput = findViewById<EditText>(R.id.emailInput)
        val locationInput = findViewById<EditText>(R.id.locationInput)
        val phoneNumberInput = findViewById<EditText>(R.id.phoneNumberInput)
        val workingHoursInput = findViewById<EditText>(R.id.workingHoursInput)
        val descriptionInput = findViewById<EditText>(R.id.descriptionInput)
        val logoInput = findViewById<EditText>(R.id.logoInput)
        val coverInput = findViewById<EditText>(R.id.coverInput)

        listOf(workingHoursInput, logoInput, coverInput).forEach { editText ->
            editText.isFocusable = false
            editText.isClickable = true
        }

        restoreDraft()

        val focusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && !isRestoring) {
                saveDraft()
            }
        }

        nameInput.onFocusChangeListener = focusChangeListener
        emailInput.onFocusChangeListener = focusChangeListener
        locationInput.onFocusChangeListener = focusChangeListener
        phoneNumberInput.onFocusChangeListener = focusChangeListener
        descriptionInput.onFocusChangeListener = focusChangeListener

        locationInput.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val addressStr = locationInput.text.toString().trim()
                if (addressStr.isNotEmpty() && !addressStr.contains(",")) {
                    fetchCoordinatesForAddress(addressStr, locationInput)
                }
                if (!isRestoring) {
                    saveDraft()
                }
            }
        }

        workingHoursInput.setOnClickListener {
            showWorkingHoursPicker(workingHoursInput)
        }

        logoInput.setOnClickListener {
            logoPickerLauncher.launch("image/*")
        }

        coverInput.setOnClickListener {
            coverPickerLauncher.launch("image/*")
        }

        val views = listOf(
            nameInput,
            emailInput,
            locationInput,
            phoneNumberInput,
            workingHoursInput,
            descriptionInput,
            logoInput,
            coverInput
        )

        views.forEach { view ->
            view.alpha = 0f
            view.translationY = 80f
        }

        findViewById<Button>(R.id.registerButton).setOnClickListener {
            clearDraft()
            createRestaurant()
        }

        root.post {
            TransitionManager.beginDelayedTransition(
                root,
                AutoTransition().apply { duration = 300 }
            )

            val constraintSet = ConstraintSet()
            constraintSet.clone(root)
            constraintSet.setGuidelinePercent(R.id.guideline, 0.05f)
            constraintSet.applyTo(root)
        }

        root.postDelayed({
            views.forEach { view ->
                view.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(200)
                    .start()
            }
        }, 200)
    }

    private fun fetchCoordinatesForAddress(addressString: String, targetEditText: EditText) {
        if (!Geocoder.isPresent()) return

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(this@RegisterBusinessActivity, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocationName(addressString, 1)

                withContext(Dispatchers.Main) {
                    if (!addresses.isNullOrEmpty()) {
                        val location = addresses[0]
                        val lat = location.latitude
                        val lon = location.longitude
                        targetEditText.setText("$lat,$lon")
                        saveDraft()
                    }
                }
            } catch (_: Exception) {}
        }
    }

    override fun onPause() {
        if (!isFinishing) {
            saveDraft()
        }
        super.onPause()
    }

    private fun saveDraft() {
        if (isRestoring) return

        val nameInput = findViewById<EditText>(R.id.nameInput) ?: return
        val emailInput = findViewById<EditText>(R.id.emailInput) ?: return
        val locationInput = findViewById<EditText>(R.id.locationInput) ?: return
        val phoneNumberInput = findViewById<EditText>(R.id.phoneNumberInput) ?: return
        val workingHoursInput = findViewById<EditText>(R.id.workingHoursInput) ?: return
        val descriptionInput = findViewById<EditText>(R.id.descriptionInput) ?: return

        getSharedPreferences(draftPrefsName, MODE_PRIVATE).edit()
            .putString("name", nameInput.text.toString())
            .putString("email", emailInput.text.toString())
            .putString("location", locationInput.text.toString())
            .putString("phone", phoneNumberInput.text.toString())
            .putString("workingHours", workingHoursInput.text.toString())
            .putString("description", descriptionInput.text.toString())
            .putString("logo", selectedLogoUri?.toString() ?: "")
            .putString("cover", selectedCoverUri?.toString() ?: "")
            .apply()
    }

    private fun restoreDraft() {
        isRestoring = true

        val prefs = getSharedPreferences(draftPrefsName, MODE_PRIVATE)
        val name = prefs.getString("name", "")
        val email = prefs.getString("email", "")
        val location = prefs.getString("location", "")
        val phone = prefs.getString("phone", "")
        val workingHours = prefs.getString("workingHours", "")
        val description = prefs.getString("description", "")
        val logo = prefs.getString("logo", "")
        val cover = prefs.getString("cover", "")

        findViewById<EditText>(R.id.nameInput).setText(name)
        findViewById<EditText>(R.id.emailInput).setText(email)
        findViewById<EditText>(R.id.locationInput).setText(location)
        findViewById<EditText>(R.id.phoneNumberInput).setText(phone)
        findViewById<EditText>(R.id.workingHoursInput).setText(workingHours)
        findViewById<EditText>(R.id.descriptionInput).setText(description)

        if (!logo.isNullOrBlank()) {
            selectedLogoUri = Uri.parse(logo)
            findViewById<EditText>(R.id.logoInput).setText("Logo selected")
        }

        if (!cover.isNullOrBlank()) {
            selectedCoverUri = Uri.parse(cover)
            findViewById<EditText>(R.id.coverInput).setText("Cover selected")
        }

        isRestoring = false
    }

    private fun clearDraft() {
        getSharedPreferences(draftPrefsName, MODE_PRIVATE).edit().clear().apply()
    }

    private fun showWorkingHoursPicker(targetEditText: EditText) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(
            this,
            { _, startHour, startMinute ->
                val startTime = String.format(Locale.getDefault(), "%02d:%02d", startHour, startMinute)

                TimePickerDialog(
                    this,
                    { _, endHour, endMinute ->
                        val endTime = String.format(Locale.getDefault(), "%02d:%02d", endHour, endMinute)
                        targetEditText.setText("$startTime - $endTime")
                        saveDraft()
                    },
                    hour,
                    minute,
                    true
                ).apply {
                    setTitle("Select Closing Time")
                    show()
                }
            },
            hour,
            minute,
            true
        ).apply {
            setTitle("Select Opening Time")
            show()
        }
    }

    private fun navigateToLogin() {
        saveDraft()
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
        finish()
    }

    private suspend fun uploadRestaurantImage(uri: Uri, fileName: String): String {
        val inputStream = contentResolver.openInputStream(uri) ?: throw IllegalStateException("Unable to open selected image")
        val file = File(cacheDir, fileName)

        inputStream.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        val requestFile = file.asRequestBody("image/*".toMediaType())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
        val response = RetrofitClient.restaurantApi.uploadImage(body)
        file.delete()

        return response.url.trim()
    }

    private fun createWorkingHours(workingHours: String): List<RestaurantWorkingHourRequest> {
        val parts = workingHours.split(" - ")

        if (parts.size != 2) {
            throw IllegalArgumentException("Please select valid working hours.")
        }

        val openTime = parts[0].trim()
        val closeTime = parts[1].trim()

        return DayOfWeek.values().map { day ->
            val closed = day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY

            RestaurantWorkingHourRequest(
                dayOfWeek = day,
                openTime = if (closed) null else openTime,
                closeTime = if (closed) null else closeTime,
                closed = closed
            )
        }
    }

    private fun createRestaurant() {
        val name = findViewById<EditText>(R.id.nameInput).text.toString()
        val email = findViewById<EditText>(R.id.emailInput).text.toString()
        val phone = findViewById<EditText>(R.id.phoneNumberInput).text.toString()
        val location = findViewById<EditText>(R.id.locationInput).text.toString()
        val workingHours = findViewById<EditText>(R.id.workingHoursInput).text.toString()

        if (name.isBlank() || email.isBlank() || phone.isBlank() || location.isBlank()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (workingHours.isBlank()) {
            Toast.makeText(this, "Please select working hours", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                Toast.makeText(this@RegisterBusinessActivity, "Uploading restaurant images...", Toast.LENGTH_SHORT).show()

                val logoPath = selectedLogoUri?.let { uploadRestaurantImage(it, "restaurant_logo.jpg") }
                val coverPath = selectedCoverUri?.let { uploadRestaurantImage(it, "restaurant_cover.jpg") }

                val request = RestaurantRequest(
                    name = name,
                    email = email,
                    phoneNumber = phone,
                    location = location,
                    workingHours = workingHours,
                    description = findViewById<EditText>(R.id.descriptionInput).text.toString(),
                    logo = logoPath,
                    cover = coverPath
                )

                val restaurant = withContext(Dispatchers.IO) {
                    RetrofitClient.restaurantApi.createRestaurant(request)
                }

                val workingHourRequests = createWorkingHours(workingHours)

                withContext(Dispatchers.IO) {
                    RetrofitClient.restaurantApi.updateMyWorkingHours(workingHourRequests)
                }

                val prefs = getSharedPreferences("auth", MODE_PRIVATE)
                prefs.edit()
                    .putLong("restaurantId", restaurant.id)
                    .putBoolean("restaurantRegistrationInProgress", false)
                    .apply()

                clearDraft()

                Toast.makeText(this@RegisterBusinessActivity, "Restaurant created!", Toast.LENGTH_LONG).show()

                val intent = Intent(this@RegisterBusinessActivity, RestaurantOwnerActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()

            } catch (e: Exception) {
                Toast.makeText(
                    this@RegisterBusinessActivity,
                    e.message ?: "Failed to create restaurant",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}