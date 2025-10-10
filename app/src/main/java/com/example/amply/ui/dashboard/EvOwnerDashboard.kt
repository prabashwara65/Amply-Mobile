package com.example.amply.ui.dashboard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.amply.R
import com.example.amply.data.UserProfileDatabaseHelper
import com.example.amply.model.ChargingStation
import com.example.amply.ui.dashboard.EvOperatorAppBar.AccountActivity
import com.example.amply.ui.reservation.MyReservationsActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

class EvOwnerDashboard : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private var googleMap: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private var currentUserId: Int = 1
    private lateinit var dbHelper: UserProfileDatabaseHelper
    private lateinit var bottomNavigation: BottomNavigationView

    private val apiUrl = "https://conor-truculent-rurally.ngrok-free.dev/api/v1/charging-stations"

    data class LocationData(
        @SerializedName("address") val address: String,
        @SerializedName("city") val city: String,
        @SerializedName("state") val state: String,
        @SerializedName("country") val country: String,
        @SerializedName("latitude") val latitude: Double,
        @SerializedName("longitude") val longitude: Double
    )

    data class ChargingStationData(
        @SerializedName("stationId") val stationId: String,
        @SerializedName("stationName") val stationName: String,
        @SerializedName("type") val type: String,
        @SerializedName("totalSlots") val totalSlots: String,
        @SerializedName("availableSlots") val availableSlots: String,
        @SerializedName("status") val status: String,
        @SerializedName("activeBookings") val activeBookings: String,
        @SerializedName("location") val location: LocationData
    )

    // Called when activity is created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ev_owner_dashboard)

        dbHelper = UserProfileDatabaseHelper(this)
        mapView = findViewById(R.id.mapView)
        bottomNavigation = findViewById(R.id.bottomNavigation)

        setupBottomNavigation()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        checkLocationPermission()
    }

    // Called when the Google Map is ready
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap?.isMyLocationEnabled = true
            showCurrentLocation()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
        fetchAndDisplayLocations()
    }

    // Fetches charging station data from API and displays them as markers on the map
    private fun fetchAndDisplayLocations() {
        CoroutineScope(Dispatchers.IO).launch {
            val response = URL(apiUrl).readText()
            val locations = Gson().fromJson(response, Array<ChargingStationData>::class.java)

            withContext(Dispatchers.Main) {
                for (loc in locations) {
                    val position = LatLng(loc.location.latitude, loc.location.longitude)
                    googleMap?.addMarker(
                        MarkerOptions()
                            .position(position)
                            .title(loc.stationId)
                            .snippet(loc.stationName)
                    )
                }
            }
        }
    }

    // Retrieves and shows the user's current location on the map
    private fun showCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val userLatLng = LatLng(it.latitude, it.longitude)
                googleMap?.addMarker(
                    MarkerOptions()
                        .position(userLatLng)
                        .title("You are here")
                )
                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
            } ?: run {
                Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Loads nearby charging stations from local database and displays markers
    private fun loadNearbyStations() {
        val stations: List<ChargingStation> = dbHelper.getAllChargingStationsList()
        for (station in stations) {
            val position = LatLng(station.latitude, station.longitude)
            googleMap?.addMarker(
                MarkerOptions()
                    .position(position)
                    .title(station.name)
                    .snippet("Available: ${station.availableSlots}/${station.totalSlots} | Type: ${station.type}")
            )
        }
    }

    // Sets up bottom navigation bar and handles item clicks for different sections
    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.nav_map
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_map -> { Toast.makeText(this, "Map", Toast.LENGTH_SHORT).show(); true }
                R.id.nav_activity -> {
                    val intent = Intent(this, MyReservationsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_saved -> { Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show(); true }
                R.id.nav_account -> {
                    // Launch AccountActivity
                    startActivity(Intent(this, AccountActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    // Checks if location permission is granted and requests it if not
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            try { googleMap?.isMyLocationEnabled = true } catch (e: SecurityException) { e.printStackTrace() }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    // Handles the result of location permission request
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
            grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            try { googleMap?.isMyLocationEnabled = true; showCurrentLocation() } catch (e: SecurityException) { e.printStackTrace() }
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    // MapView lifecycle
    override fun onResume() { super.onResume(); mapView.onResume() }
    override fun onPause() { super.onPause(); mapView.onPause() }
    override fun onStart() { super.onStart(); mapView.onStart() }
    override fun onStop() { super.onStop(); mapView.onStop() }
    override fun onDestroy() { super.onDestroy(); mapView.onDestroy() }
    override fun onLowMemory() { super.onLowMemory(); mapView.onLowMemory() }
}

