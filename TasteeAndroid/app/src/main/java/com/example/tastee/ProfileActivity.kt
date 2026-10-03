package com.example.tastee

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tastee.adapter.DeliveryOrderAdapter
import com.example.tastee.dto.UpdateUserRequest
import com.example.tastee.network.RetrofitClient
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class ProfileActivity : AppCompatActivity() {

    private lateinit var profileImage: ShapeableImageView
    private lateinit var changePictureButton: Button

    private lateinit var usernameInput: EditText
    private lateinit var nameInput: EditText
    private lateinit var surnameInput: EditText
    private lateinit var emailInput: EditText

    private lateinit var saveButton: Button
    private lateinit var logoutButton: Button

    private lateinit var detailsTab: TextView
    private lateinit var ordersTab: TextView

    private lateinit var tabsLayout: android.widget.LinearLayout

    private lateinit var ordersRecyclerView: RecyclerView
    private lateinit var orderAdapter: DeliveryOrderAdapter
    private lateinit var emptyOrdersText: TextView

    private var selectedImageUri: Uri? = null

    companion object {
        private const val PICK_IMAGE_REQUEST = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        profileImage = findViewById(R.id.profileImage)
        changePictureButton = findViewById(R.id.changePictureButton)
        usernameInput = findViewById(R.id.usernameInput)
        nameInput = findViewById(R.id.nameInput)
        surnameInput = findViewById(R.id.surnameInput)
        emailInput = findViewById(R.id.emailInput)
        saveButton = findViewById(R.id.saveButton)
        logoutButton = findViewById(R.id.logoutButton)

        detailsTab = findViewById(R.id.detailsTab)
        ordersTab = findViewById(R.id.ordersTab)
        tabsLayout = findViewById(R.id.tabsLayout)

        ordersRecyclerView = findViewById(R.id.ordersRecyclerView)
        emptyOrdersText = findViewById(R.id.emptyOrdersText)

        ordersRecyclerView.layoutManager = LinearLayoutManager(this)
        ordersRecyclerView.setHasFixedSize(false)

        loadUser()

        changePictureButton.setOnClickListener {
            openImagePicker()
        }

        saveButton.setOnClickListener {
            updateUser()
        }

        logoutButton.setOnClickListener {
            logout()
        }

        detailsTab.setOnClickListener {
            showDetailsTab()
        }

        ordersTab.setOnClickListener {
            showOrdersTab()
        }

        showDetailsTab()
    }

    private fun setupTabsForRole(role: String?) {
        if (role == "ROLE_RESTAURANT") {
            ordersTab.visibility = View.GONE
            val params = detailsTab.layoutParams as android.widget.LinearLayout.LayoutParams
            params.weight = 2f
            detailsTab.layoutParams = params
        } else {
            ordersTab.visibility = View.VISIBLE
            val params = detailsTab.layoutParams as android.widget.LinearLayout.LayoutParams
            params.weight = 1f
            detailsTab.layoutParams = params
        }
    }

    private fun showDetailsTab() {
        findViewById<View>(R.id.detailsScrollView).visibility = View.VISIBLE
        ordersRecyclerView.visibility = View.GONE
        emptyOrdersText.visibility = View.GONE

        detailsTab.setTextColor(getColor(android.R.color.white))
        detailsTab.setTypeface(null, android.graphics.Typeface.BOLD)

        if (ordersTab.visibility == View.VISIBLE) {
            ordersTab.setTextColor(getColor(android.R.color.darker_gray))
            ordersTab.setTypeface(null, android.graphics.Typeface.NORMAL)
        }
    }

    private fun showOrdersTab() {
        if (ordersTab.visibility != View.VISIBLE) {
            return
        }

        findViewById<View>(R.id.detailsScrollView).visibility = View.GONE

        ordersTab.setTextColor(getColor(android.R.color.white))
        ordersTab.setTypeface(null, android.graphics.Typeface.BOLD)

        detailsTab.setTextColor(getColor(android.R.color.darker_gray))
        detailsTab.setTypeface(null, android.graphics.Typeface.NORMAL)

        loadOrders()
    }

    private fun loadOrders() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val user = RetrofitClient.userApi.getCurrentUser()
                val isDriver = user.role == "ROLE_DELIVERY"

                val orders = if (isDriver) {
                    RetrofitClient.orderApi.getDeliveryDriverOrders()
                } else {
                    RetrofitClient.orderApi.getMyOrders()
                }

                withContext(Dispatchers.Main) {
                    orderAdapter = DeliveryOrderAdapter(emptyList(), { order ->
                        val intent = Intent(this@ProfileActivity, DeliveryOrderDetailsActivity::class.java)
                        intent.putExtra("ORDER_ID", order.id)
                        intent.putExtra("IS_DRIVER", isDriver)
                        startActivity(intent)
                    }, isDriver)

                    ordersRecyclerView.adapter = orderAdapter
                    orderAdapter.updateOrders(orders)

                    if (orders.isEmpty()) {
                        ordersRecyclerView.visibility = View.GONE
                        emptyOrdersText.visibility = View.VISIBLE
                    } else {
                        ordersRecyclerView.visibility = View.VISIBLE
                        emptyOrdersText.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ProfileActivity,
                        "Failed to load orders: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun loadUser() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val user = RetrofitClient.userApi.getCurrentUser()

                withContext(Dispatchers.Main) {
                    usernameInput.setText(user.username)
                    emailInput.setText(user.email)
                    nameInput.setText(user.name ?: "")
                    surnameInput.setText(user.surname ?: "")

                    if (!user.profilePicture.isNullOrBlank()) {
                        val imageUrl = "http://10.0.2.2:8080${user.profilePicture}"
                        Glide.with(this@ProfileActivity)
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_person)
                            .error(R.drawable.ic_person)
                            .into(profileImage)
                    }

                    setupTabsForRole(user.role)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ProfileActivity,
                        "Failed to load profile: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            selectedImageUri?.let { uri ->
                profileImage.setImageURI(uri)
                uploadProfilePicture(uri)
            }
        }
    }

    private fun uploadProfilePicture(uri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val file = File(cacheDir, "profile_picture.jpg")

                inputStream?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                val requestFile = file.asRequestBody("image/*".toMediaType())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                RetrofitClient.userApi.updateProfilePicture(body)

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ProfileActivity,
                        "Profile picture updated",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ProfileActivity,
                        "Failed to upload picture: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun updateUser() {
        val request = UpdateUserRequest(
            username = usernameInput.text.toString().trim(),
            email = emailInput.text.toString().trim(),
            name = nameInput.text.toString().trim(),
            surname = surnameInput.text.toString().trim()
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val updatedUser = RetrofitClient.userApi.updateCurrentUser(request)

                withContext(Dispatchers.Main) {
                    getSharedPreferences("auth", MODE_PRIVATE)
                        .edit()
                        .putString("username", updatedUser.username)
                        .apply()

                    Toast.makeText(
                        this@ProfileActivity,
                        "Profile updated successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ProfileActivity,
                        "Failed to update profile: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun logout() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                RetrofitClient.authApi.logout()
            } catch (e: Exception) {
            }

            withContext(Dispatchers.Main) {
                getSharedPreferences("auth", MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply()

                val intent = Intent(this@ProfileActivity, LoginActivity::class.java)
                intent.putExtra("ANIMATE_FROM_LOGOUT", true)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }
}