package com.example.amply

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.annotations.IconFactory
import android.graphics.Color
import com.google.android.material.tabs.TabLayout
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.google.android.material.bottomnavigation.BottomNavigationView


class HomeActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private var mapboxMap: MapboxMap? = null
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var reservationRecyclerView: RecyclerView
    private lateinit var reservationAdapter: ReservationAdapter
    private lateinit var tabLayout: TabLayout
    private lateinit var bottomNavigation: BottomNavigationView

    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private var currentUserId: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Mapbox.getInstance(this)

        setContentView(R.layout.activity_home)

        dbHelper = DatabaseHelper(this)

        // Initialize views
        tabLayout = findViewById(R.id.tabLayout)
        reservationRecyclerView = findViewById(R.id.reservationRecyclerView)
        mapView = findViewById(R.id.mapView)
        bottomNavigation = findViewById(R.id.bottomNavigation)

        reservationRecyclerView.layoutManager = LinearLayoutManager(this)

        setupTabs()
        setupBottomNavigation()

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { map ->
            mapboxMap = map
            map.setStyle(Style.Builder().fromUri("https://demotiles.maplibre.org/style.json")) { style ->
                setupMap()
            }
        }

        checkLocationPermission()
        loadReservations("pending")
    }

    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.nav_map
        
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_map -> {
                    // Already on map screen
                    Toast.makeText(this, "Map", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_activity -> {
                    // Navigate to activity screen
                    Toast.makeText(this, "Activity - Coming Soon", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_saved -> {
                    // Navigate to saved screen
                    Toast.makeText(this, "Saved - Coming Soon", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_account -> {
                    // Navigate to account screen
                    Toast.makeText(this, "Account - Coming Soon", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Pending"))
        tabLayout.addTab(tabLayout.newTab().setText("Confirmed"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> loadReservations("pending")
                    1 -> loadReservations("confirmed")
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun loadReservations(status: String) {
        val reservations = dbHelper.getReservationsByStatus(currentUserId, status)
        reservationAdapter = ReservationAdapter(reservations) { reservation ->
            Toast.makeText(this, "Reservation: ${reservation.stationName}", Toast.LENGTH_SHORT).show()
        }
        reservationRecyclerView.adapter = reservationAdapter
    }

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
        val iconFactory = IconFactory.getInstance(this)

        for (station in stations) {
            val position = LatLng(station.latitude, station.longitude)
            val markerOptions = MarkerOptions()
                .position(position)
                .title(station.name)
                .snippet("Available: ${station.availableSlots}/${station.totalSlots} | Type: ${station.type}")

            mapboxMap?.addMarker(markerOptions)
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            }
        }
    }


    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }
}
