package com.example.amply.ui.EVOperatorApp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.amply.R
import com.example.amply.model.Reservation
import com.example.amply.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * BookingDetailsActivity
 * Displays fake booking details for demonstration purposes
 * Shows booking information and allows user to confirm the data
 */
class BookingDetailsActivity : AppCompatActivity() {

    // UI Components
    private lateinit var tvBookingId: TextView
    private lateinit var tvOwnerName: TextView
    private lateinit var tvBookingDate: TextView
    private lateinit var tvBookingTime: TextView
    private lateinit var tvSlotNumber: TextView
    private lateinit var tvChargingDuration: TextView
    private lateinit var tvVehicleModel: TextView
    private lateinit var btnConfirmData: Button
    private lateinit var btnProceedToFinalize: Button
    
    // Data
    private var currentReservation: Reservation? = null

    /**
     * onCreate method - initializes the activity and sets up UI components
     * @param savedInstanceState Bundle containing the activity's previously saved state
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking_details)

        // Initialize UI components
        initializeViews()

        // Load booking data from API
        loadBookingData()

        // Set up button click listeners
        setupClickListeners()
    }

    /**
     * Initialize all UI views by finding them from the layout
     */
    private fun initializeViews() {
        tvBookingId = findViewById(R.id.tvBookingId)
        tvOwnerName = findViewById(R.id.tvOwnerName)
        tvBookingDate = findViewById(R.id.tvBookingDate)
        tvBookingTime = findViewById(R.id.tvBookingTime)
        tvSlotNumber = findViewById(R.id.tvSlotNumber)
        tvChargingDuration = findViewById(R.id.tvChargingDuration)
        tvVehicleModel = findViewById(R.id.tvVehicleModel)
        btnConfirmData = findViewById(R.id.btnConfirmData)
        btnProceedToFinalize = findViewById(R.id.btnProceedToFinalize)
    }

    /**
     * Load booking data from intent extras
     * Gets reservation data passed from QR scanner
     */
    private fun loadBookingData() {
        // Check if we have data from intent extras
        if (intent.hasExtra("reservation_code")) {
            // Load from intent extras (passed from QR scanner)
            val reservation = Reservation(
                id = intent.getStringExtra("reservation_id"),
                reservationCode = intent.getStringExtra("reservation_code") ?: "",
                fullName = intent.getStringExtra("full_name") ?: "",
                nic = intent.getStringExtra("nic"),
                vehicleNumber = intent.getStringExtra("vehicle_number") ?: "",
                stationId = intent.getStringExtra("station_id") ?: "",
                stationName = intent.getStringExtra("station_name") ?: "",
                slotNo = intent.getIntExtra("slot_no", 0),
                reservationDate = intent.getStringExtra("reservation_date") ?: "",
                startTime = intent.getStringExtra("start_time") ?: "",
                endTime = intent.getStringExtra("end_time") ?: "",
                status = intent.getStringExtra("status") ?: "Pending",
                qrCode = intent.getStringExtra("qr_code")
            )
            currentReservation = reservation
            displayBookingData(reservation)
        } else {
            // Fallback: try to fetch from API (for direct access)
            val reservationId = intent.getStringExtra("reservation_id") ?: "BK-2024-10-001"
            fetchReservationData(reservationId)
        }
    }

    /**
     * Fetch reservation data from the backend API
     * Follows the same pattern as MainActivity.kt for consistency
     * @param reservationId The reservation ID to fetch
     */
    private fun fetchReservationData(reservationId: String) {
        // Use the same API client pattern as MainActivity
        val apiService = ApiClient.reservationApiService
        
        apiService.getReservationById(reservationId).enqueue(object : Callback<Reservation> {
            override fun onResponse(call: Call<Reservation>, response: Response<Reservation>) {
                if (response.isSuccessful) {
                    val reservation = response.body()
                    if (reservation != null) {
                        // Successfully fetched reservation data
                        currentReservation = reservation
                        displayBookingData(reservation)
                    } else {
                        showError("No booking found for the given ID")
                    }
                } else {
                    // API call failed with error code
                    showError("Failed to fetch booking: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Reservation>, t: Throwable) {
                // Network or other error
                showError("Network error: ${t.message}")
            }
        })
    }

    /**
     * Display the fetched booking data
     * @param reservation The reservation data from the API
     */
    private fun displayBookingData(reservation: Reservation) {
        tvBookingId.text = "${reservation.id ?: "N/A"}"
        tvOwnerName.text = "${reservation.fullName}"
        tvBookingDate.text = "${reservation.reservationDate}"
        tvBookingTime.text = "${reservation.startTime} - ${reservation.endTime}"
        tvSlotNumber.text = "${reservation.slotNo}"
        tvChargingDuration.text = "${calculateDuration(reservation.startTime, reservation.endTime)}"
        tvVehicleModel.text = "${reservation.vehicleNumber}"
    }

    /**
     * Calculate duration between start and end time
     * @param startTime Start time string
     * @param endTime End time string
     * @return Duration string
     */
    private fun calculateDuration(startTime: String, endTime: String): String {
        // Simple duration calculation - in a real app, you'd parse the times properly
        return "2 Hours" // Placeholder for demo
    }

    /**
     * Show error message to user
     * @param message Error message to display
     */
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        // Set default values on error
        tvBookingId.text = "Error loading data"
        tvOwnerName.text = "Error loading data"
        tvBookingDate.text = "Error loading data"
        tvBookingTime.text = "Error loading data"
        tvSlotNumber.text = "Error loading data"
        tvChargingDuration.text = "Error loading data"
        tvVehicleModel.text = "Error loading data"
    }

    /**
     * Set up click listeners for the confirm button
     */
    private fun setupClickListeners() {
        btnConfirmData.setOnClickListener {
            confirmBookingData()
        }
        
        btnProceedToFinalize.setOnClickListener {
            proceedToFinalize()
        }
    }

    /**
     * Handle booking data confirmation
     * Shows a toast message to indicate successful confirmation
     */
    private fun confirmBookingData() {
        Toast.makeText(
            this,
            "Booking data confirmed successfully!",
            Toast.LENGTH_SHORT
        ).show()
        
        // Enable proceed to finalize button
        btnProceedToFinalize.isEnabled = true
        btnProceedToFinalize.alpha = 1.0f
    }

    /**
     * Navigate to Finalize Operation Activity
     * Passes the reservation data
     */
    private fun proceedToFinalize() {
        if (currentReservation != null) {
            val intent = Intent(this, FinalizeOperationActivity::class.java)
            // Pass the entire reservation as individual extras
            intent.putExtra("reservation_id", currentReservation?.id)
            intent.putExtra("reservation_code", currentReservation?.reservationCode)
            intent.putExtra("full_name", currentReservation?.fullName)
            intent.putExtra("nic", currentReservation?.nic)
            intent.putExtra("vehicle_number", currentReservation?.vehicleNumber)
            intent.putExtra("station_id", currentReservation?.stationId)
            intent.putExtra("station_name", currentReservation?.stationName)
            intent.putExtra("slot_no", currentReservation?.slotNo)
            intent.putExtra("reservation_date", currentReservation?.reservationDate)
            intent.putExtra("start_time", currentReservation?.startTime)
            intent.putExtra("end_time", currentReservation?.endTime)
            intent.putExtra("status", currentReservation?.status)
            intent.putExtra("qr_code", currentReservation?.qrCode)
            startActivity(intent)
        } else {
            Toast.makeText(this, "No reservation data available", Toast.LENGTH_SHORT).show()
        }
    }
}

