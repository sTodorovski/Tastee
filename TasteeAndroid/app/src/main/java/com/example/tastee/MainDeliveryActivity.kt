package com.example.tastee

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tastee.adapter.DeliveryOrderAdapter
import com.example.tastee.network.RetrofitClient
import com.example.tastee.utils.DeliveryNotificationHelper
import com.example.tastee.utils.LocationManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.launch

class MainDeliveryActivity : AppCompatActivity() {
    private lateinit var profileButton: ShapeableImageView
    private lateinit var orderRecyclerView: RecyclerView
    private lateinit var orderAdapter: DeliveryOrderAdapter

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                Toast.makeText(this, "Notification permission is required for delivery notifications.", Toast.LENGTH_LONG).show()
            }
        }

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (fineGranted || coarseGranted) {
                getCurrentLocation()
            } else {
                Toast.makeText(this, "Location permission was denied.", Toast.LENGTH_LONG).show()
            }
        }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_delivery)

        DeliveryNotificationHelper.createNotificationChannel(this)
        requestNotificationPermission()

        profileButton = findViewById(R.id.profileButton)
        orderRecyclerView = findViewById(R.id.orderRecyclerView)

        orderAdapter = DeliveryOrderAdapter(
            emptyList(),
            { order ->
                val intent = Intent(this, DeliveryOrderDetailsActivity::class.java)
                intent.putExtra("ORDER_ID", order.id)
                intent.putExtra("IS_DRIVER", true)
                startActivity(intent)
            },
            true
        )

        orderRecyclerView.layoutManager = LinearLayoutManager(this)
        orderRecyclerView.adapter = orderAdapter

        loadAvailableOrders()
        loadProfilePicture()

        profileButton.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun requestLocationPermission() {
        if (LocationManager.hasLocationPermission(this)) {
            getCurrentLocation()
            return
        }

        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun getCurrentLocation() {
        val fineGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fineGranted && !coarseGranted) return

        val cancellationTokenSource = CancellationTokenSource()

        fusedLocationClient
            .getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            )
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    LocationManager.setUserLocation(location)
                    Toast.makeText(this, "Restaurants sorted by distance.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Could not determine your current location.", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to get your current location.", Toast.LENGTH_LONG).show()
            }
    }

    override fun onResume() {
        super.onResume()
        loadProfilePicture()
        loadAvailableOrders()
    }

    private fun loadProfilePicture() {
        lifecycleScope.launch {
            try {
                val user = RetrofitClient.userApi.getCurrentUser()
                if (!user.profilePicture.isNullOrBlank()) {
                    val imageUrl = "http://10.0.2.2:8080${user.profilePicture}"
                    Glide.with(this@MainDeliveryActivity)
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

    private fun loadAvailableOrders() {
        lifecycleScope.launch {
            try {
                val orders = RetrofitClient.orderApi.getAvailableOrders()
                orderAdapter.updateOrders(orders)
            } catch (e: Exception) {
                Toast.makeText(this@MainDeliveryActivity, "Failed to load delivery orders.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}