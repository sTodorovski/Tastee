package com.example.tastee

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.tastee.adapter.MainPagerAdapter
import com.example.tastee.dto.CartManager
import com.example.tastee.network.RetrofitClient
import com.example.tastee.utils.LocationManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var cartButton: ImageButton
    private lateinit var cartBadge: TextView
    private lateinit var mapButton: FloatingActionButton
    private lateinit var locationButton: FloatingActionButton
    private lateinit var profileButton: ShapeableImageView
    private lateinit var searchView: SearchView
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cartButton = findViewById(R.id.cartButton)
        cartBadge = findViewById(R.id.cartBadge)
        profileButton = findViewById(R.id.profileButton)
        searchView = findViewById(R.id.searchView)
        locationButton = findViewById(R.id.locationButton)
        mapButton = findViewById(R.id.mapButton)
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)

        val pagerAdapter = MainPagerAdapter(this)
        viewPager.adapter = pagerAdapter
        viewPager.setBackgroundColor(ContextCompat.getColor(this, R.color.black))
        viewPager.isUserInputEnabled = false

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Restaurants"
                1 -> tab.text = "Categories"
            }
        }.attach()

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterActiveFragment(query.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterActiveFragment(newText.orEmpty())
                return true
            }
        })

        cartButton.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }

        loadProfilePicture()
        profileButton.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        locationButton.setOnClickListener {
            requestLocationPermission()
        }

        mapButton.setOnClickListener {
            startActivity(Intent(this, MapActivity::class.java))
        }

        updateCartBadge()
    }

    private fun filterActiveFragment(query: String) {
        val fragmentTag = "f${viewPager.currentItem}"
        val currentFragment = supportFragmentManager.findFragmentByTag(fragmentTag)
        if (currentFragment is Searchable) {
            currentFragment.onSearchQuery(query)
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
                    refreshCurrentTab()
                } else {
                    Toast.makeText(this, "Could not determine your current location.", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to get your current location.", Toast.LENGTH_LONG).show()
            }
    }

    private fun refreshCurrentTab() {
        val currentPosition = viewPager.currentItem
        viewPager.adapter = MainPagerAdapter(this)
        viewPager.currentItem = currentPosition
    }

    fun updateCartBadge() {
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
    }

    private fun loadProfilePicture() {
        lifecycleScope.launch {
            try {
                val user = RetrofitClient.userApi.getCurrentUser()
                if (!user.profilePicture.isNullOrBlank()) {
                    val imageUrl = "http://10.0.2.2:8080${user.profilePicture}"
                    Glide.with(this@MainActivity)
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