package com.example.tastee.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat

object LocationManager {
    var userLocation: Location? = null
        private set

    var locationSortingEnabled: Boolean = false
        private set

    fun hasLocationPermission(context: Context): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineGranted || coarseGranted
    }

    fun setUserLocation(location: Location) {
        userLocation = location
        locationSortingEnabled = true
    }

    fun clearLocation() {
        userLocation = null
        locationSortingEnabled = false
    }
}