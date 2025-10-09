package com.example.amply.ui.dashboard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.amply.R
import com.example.amply.data.UserProfileDatabaseHelper
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.example.amply.ui.dashboard.EvOperatorAppBar.AccountActivity


class EvOwnerDashboard : AppCompatActivity() {

    private lateinit var mapView: MapView
    private var mapboxMap: MapboxMap? = null
    private lateinit var dbHelper: UserProfileDatabaseHelper
    private lateinit var bottomNavigation: BottomNavigationView

    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Mapbox.getInstance(this)
        setContentView(R.layout.activity_ev_owner_dashboard)

        dbHelper = UserProfileDatabaseHelper(this)

        // Initialize views
        mapView = findViewById(R.id.mapView)
        bottomNavigation = findViewById(R.id.bottomNavigation)

        setupBottomNavigation()

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { map ->
            mapboxMap = map
            map.setStyle(Style.Builder().fromUri("https://demotiles.maplibre.org/style.json")) {
                setupMap()
            }
        }

        checkLocationPermission()
    }

    // -------------------------
    // Bottom navigation
    // -------------------------
    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.nav_map

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_map -> {
                    Toast.makeText(this, "Map", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_activity -> {
                    Toast.makeText(this, "Activity - Coming Soon", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_saved -> {
                    Toast.makeText(this, "Saved - Coming Soon", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_account -> {
                    // Launch AccountActivity
                    startActivity(Intent(this, AccountActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }


    // -------------------------
    // Map setup
    // -------------------------
    private fun setupMap() {
        mapboxMap?.let { map ->
            val defaultLocation = LatLng(6.9271, 79.8612)
            val position = CameraPosition.Builder()
                .target(defaultLocation)
                .zoom(12.0)
                .build()
            map.cameraPosition = position

            addUserLocationMarker(defaultLocation.latitude, defaultLocation.longitude)
            loadNearbyStations()
        }
    }

    private fun addUserLocationMarker(lat: Double, lng: Double) {
        mapboxMap?.addMarker(
            MarkerOptions()
                .position(LatLng(lat, lng))
                .title("You are here")
        )
    }

    private fun loadNearbyStations() {
        val stations = dbHelper.getAllChargingStationsList()
        for (station in stations) {
            val position = LatLng(station.latitude, station.longitude)
            val markerOptions = MarkerOptions()
                .position(position)
                .title(station.name)
                .snippet("Available: ${station.availableSlots}/${station.totalSlots} | Type: ${station.type}")

            mapboxMap?.addMarker(markerOptions)
        }
    }

    // -------------------------
    // Permissions
    // -------------------------
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
            grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission granted
        }
    }

    // -------------------------
    // MapView lifecycle
    // -------------------------
    override fun onStart() { super.onStart(); mapView.onStart() }
    override fun onResume() { super.onResume(); mapView.onResume() }
    override fun onPause() { super.onPause(); mapView.onPause() }
    override fun onStop() { super.onStop(); mapView.onStop() }
    override fun onSaveInstanceState(outState: Bundle) { super.onSaveInstanceState(outState); mapView.onSaveInstanceState(outState) }
    override fun onLowMemory() { super.onLowMemory(); mapView.onLowMemory() }
    override fun onDestroy() { super.onDestroy(); mapView.onDestroy() }
}
