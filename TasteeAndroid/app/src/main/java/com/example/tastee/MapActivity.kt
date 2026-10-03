package com.example.tastee

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.tastee.network.RetrofitClient
import com.example.tastee.utils.LocationManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

class MapActivity : AppCompatActivity() {
    private lateinit var map: MapView
    private var userMarker: Marker? = null
    private var deliveryOrderId: Long = -1L
    private var isSelectMode: Boolean = false

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences("osmdroid", MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = "Tastee/1.0 (${packageName})"

        setContentView(R.layout.activity_map)

        map = findViewById(R.id.map)
        deliveryOrderId = intent.getLongExtra("DELIVERY_ORDER_ID", -1L)
        isSelectMode = intent.getBooleanExtra("IS_SELECT_MODE", false)

        map.setTileSource(TileSourceFactory.MAPNIK)

        val zoomInButton = findViewById<Button>(R.id.zoomInButton)
        val zoomOutButton = findViewById<Button>(R.id.zoomOutButton)

        map.setMultiTouchControls(true)
        zoomInButton.setOnClickListener { map.controller.zoomIn() }
        zoomOutButton.setOnClickListener { map.controller.zoomOut() }

        map.zoomController.setVisibility(
            org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER
        )

        map.controller.setCenter(GeoPoint(41.9981, 21.4254))
        map.controller.setZoom(13.0)

        if (isSelectMode) {
            setupMapSelectionMode()
        } else if (deliveryOrderId != -1L) {
            loadDeliveryMap()
        } else {
            loadRestaurants()
            showExistingUserLocation()
        }
    }

    private fun setupMapSelectionMode() {
        Toast.makeText(this, "Tap anywhere on the map to set location", Toast.LENGTH_SHORT).show()

        val eventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                p?.let { point ->
                    val resultIntent = Intent().apply {
                        putExtra("SELECTED_LOCATION", "${point.latitude},${point.longitude}")
                    }
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                }
                return true
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                return false
            }
        })

        map.overlays.clear()
        map.overlays.add(eventsOverlay)
        showExistingUserLocation()
    }

    private fun showExistingUserLocation() {
        if (LocationManager.hasLocationPermission(this)) {
            getCurrentLocation()
        } else {
            Toast.makeText(
                this,
                "Use the location button on the main screen to enable your location.",
                Toast.LENGTH_LONG
            ).show()
        }
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
                    showUserLocation(location.latitude, location.longitude)
                }
            }
    }

    private fun showUserLocation(latitude: Double, longitude: Double) {
        if (map.width == 0 || map.height == 0) {
            map.post { showUserLocation(latitude, longitude) }
            return
        }

        val userLocation = GeoPoint(latitude, longitude)
        userMarker?.let { map.overlays.remove(it) }

        val marker = Marker(map)
        marker.position = userLocation
        marker.title = "You are here"
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

        if (isSelectMode) {
            marker.setOnMarkerClickListener { _, _ ->
                val resultIntent = Intent().apply {
                    putExtra("SELECTED_LOCATION", "${latitude},${longitude}")
                }
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
                true
            }
        }

        userMarker = marker
        map.overlays.add(marker)
        map.controller.animateTo(userLocation)
        map.controller.setZoom(15.0)
        map.invalidate()
    }

    private fun loadRestaurants() {
        if (isSelectMode) return

        lifecycleScope.launch {
            try {
                val restaurants = RetrofitClient.restaurantApi.getRestaurants()
                for (restaurant in restaurants) {
                    val coordinates = parseCoordinates(restaurant.location)
                    if (coordinates != null) {
                        addRestaurantMarker(restaurant.id, restaurant.name, coordinates)
                    }
                }
                map.invalidate()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun addRestaurantMarker(restaurantId: Long, restaurantName: String, coordinates: GeoPoint) {
        val marker = Marker(map)
        marker.position = coordinates
        marker.title = restaurantName
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.setOnMarkerClickListener { _, _ ->
            val intent = Intent(this, RestaurantActivity::class.java)
            intent.putExtra("restaurantId", restaurantId)
            startActivity(intent)
            true
        }
        map.overlays.add(marker)
    }

    private fun parseCoordinates(location: String?): GeoPoint? {
        if (location.isNullOrBlank()) return null
        return try {
            val parts = location.split(",")
            if (parts.size != 2) return null
            val latitude = parts[0].trim().toDouble()
            val longitude = parts[1].trim().toDouble()
            if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) return null
            GeoPoint(latitude, longitude)
        } catch (e: Exception) {
            null
        }
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        map.onDetach()
    }

    private fun loadDeliveryMap() {
        lifecycleScope.launch {
            try {
                val order = RetrofitClient.orderApi.getOrderById(deliveryOrderId)
                geocodeDeliveryAddress(order.deliveryAddress)
            } catch (e: Exception) {
                finish()
            }
        }
    }

    private fun geocodeDeliveryAddress(address: String) {
        val geocoder = Geocoder(this)
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val results = geocoder.getFromLocationName(address, 1)
                if (!results.isNullOrEmpty()) {
                    val location = results[0]
                    withContext(Dispatchers.Main) {
                        showDeliveryLocation(location.latitude, location.longitude, address)
                    }
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    private fun showDeliveryLocation(latitude: Double, longitude: Double, address: String) {
        val deliveryLocation = GeoPoint(latitude, longitude)
        val marker = Marker(map)
        marker.position = deliveryLocation
        marker.title = "Delivery address"
        marker.snippet = address
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

        map.overlays.add(marker)
        map.controller.animateTo(deliveryLocation)
        map.invalidate()
    }
}