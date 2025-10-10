package com.example.amply.ui.EVOperatorApp

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.amply.R
import com.example.amply.model.Reservation
import com.example.amply.model.ReservationStatusUpdate
import com.example.amply.network.ApiClient
import com.example.amply.data.ReservationDatabaseHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

/**
 * FinalizeOperationActivity
 * Displays a booking summary and allows the operator to finalize the operation
 * Shows comprehensive details before final confirmation
 */
class FinalizeOperationActivity : AppCompatActivity() {

    // UI Components
    private lateinit var tvSummaryBookingId: TextView
    private lateinit var tvSummaryOwner: TextView
    private lateinit var tvSummaryDate: TextView
    private lateinit var tvSummaryTime: TextView
    private lateinit var tvSummarySlot: TextView
    private lateinit var tvSummaryDuration: TextView
    private lateinit var tvSummaryVehicle: TextView
    private lateinit var tvSummaryChargeAmount: TextView
    private lateinit var tvSummaryTotalCost: TextView
    private lateinit var btnFinalizeOperation: Button
    
    // Data
    private var currentReservation: Reservation? = null
    
    // Database Helper
    private lateinit var dbHelper: ReservationDatabaseHelper

    /**
     * onCreate method - initializes the activity and sets up UI components
     * @param savedInstanceState Bundle containing the activity's previously saved state
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finalize_operation)

        // Initialize database helper
        dbHelper = ReservationDatabaseHelper(this)

        // Initialize UI components
        initializeViews()

        // Load booking summary data from API
        loadBookingSummary()

        // Set up button click listeners
        setupClickListeners()
    }

    /**
     * Initialize all UI views by finding them from the layout
     */
    private fun initializeViews() {
        tvSummaryBookingId = findViewById(R.id.tvSummaryBookingId)
        tvSummaryOwner = findViewById(R.id.tvSummaryOwner)
        tvSummaryDate = findViewById(R.id.tvSummaryDate)
        tvSummaryTime = findViewById(R.id.tvSummaryTime)
        tvSummarySlot = findViewById(R.id.tvSummarySlot)
        tvSummaryDuration = findViewById(R.id.tvSummaryDuration)
        tvSummaryVehicle = findViewById(R.id.tvSummaryVehicle)
        tvSummaryChargeAmount = findViewById(R.id.tvSummaryChargeAmount)
        tvSummaryTotalCost = findViewById(R.id.tvSummaryTotalCost)
        btnFinalizeOperation = findViewById(R.id.btnFinalizeOperation)
    }

    /**
     * Load booking summary data from intent extras
     * Gets reservation data passed from BookingDetailsActivity
     */
    private fun loadBookingSummary() {
        // Check if we have data from intent extras
        if (intent.hasExtra("reservation_code")) {
            // Load from intent extras (passed from BookingDetailsActivity)
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
            displayBookingSummary(reservation)
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
                        displayBookingSummary(reservation)
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
     * Display the fetched booking summary data
     * @param reservation The reservation data from the API
     */
    private fun displayBookingSummary(reservation: Reservation) {
        tvSummaryBookingId.text = "${reservation.id ?: "N/A"}"
        tvSummaryOwner.text = "${reservation.fullName}"
        tvSummaryDate.text = "${reservation.reservationDate}"
        tvSummaryTime.text = "${reservation.startTime} - ${reservation.endTime}"
        tvSummarySlot.text = "${reservation.slotNo}"
        tvSummaryDuration.text = "${calculateDuration(reservation.startTime, reservation.endTime)}"
        tvSummaryVehicle.text = "${reservation.vehicleNumber}"
        tvSummaryChargeAmount.text = "45.5 kWh" // Placeholder - would come from charging data
        tvSummaryTotalCost.text = "$22.75" // Placeholder - would be calculated
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
        tvSummaryBookingId.text = "Error loading data"
        tvSummaryOwner.text = "Error loading data"
        tvSummaryDate.text = "Error loading data"
        tvSummaryTime.text = "Error loading data"
        tvSummarySlot.text = "Error loading data"
        tvSummaryDuration.text = "Error loading data"
        tvSummaryVehicle.text = "Error loading data"
        tvSummaryChargeAmount.text = "Error loading data"
        tvSummaryTotalCost.text = "Error loading data"
    }

    /**
     * Set up click listeners for the finalize button
     */
    private fun setupClickListeners() {
        btnFinalizeOperation.setOnClickListener {
            finalizeOperation()
        }
    }

    /**
     * Handle operation finalization
     * Calls the backend API to mark the booking as complete
     */
    private fun finalizeOperation() {
        val reservation = currentReservation
        if (reservation == null) {
            Toast.makeText(this, "No reservation data available", Toast.LENGTH_SHORT).show()
            return
        }

        // Disable button to prevent multiple clicks
        btnFinalizeOperation.isEnabled = false
        btnFinalizeOperation.text = "Finalizing..."

        // Update reservation status to "Completed"
        updateReservationStatus(reservation.id ?: "", "Completed")
    }

    /**
     * Update reservation status via API call
     * @param reservationId The reservation ID to update
     * @param status The new status to set
     */
    private fun updateReservationStatus(reservationId: String, status: String) {
        val apiService = ApiClient.reservationApiService
        val statusUpdate = ReservationStatusUpdate(status)
        
        // Create updated reservation object
        val updatedReservation = currentReservation?.copy(status = status)
        
        if (updatedReservation != null) {
            apiService.updateReservation(reservationId, updatedReservation).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        // Save to local database
                        saveToLocalDatabase(updatedReservation)
                        
                        // Success - show confirmation
                        Toast.makeText(
                            this@FinalizeOperationActivity,
                            "Operation Finalized Successfully!",
                            Toast.LENGTH_LONG
                        ).show()
                        
                        // Navigate back or finish
                        finish()
                    } else {
                        // API error - but still save locally
                        saveToLocalDatabase(updatedReservation)
                        
                        // Show error but confirm local save
                        val errorBody = response.errorBody()?.string()
                        Toast.makeText(
                            this@FinalizeOperationActivity,
                            "Saved locally. API error: ${response.code()}",
                            Toast.LENGTH_LONG
                        ).show()
                        
                        println("API Error ${response.code()}: $errorBody")
                        
                        // Still finish since we saved locally
                        finish()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    // Network error - but still save to local database
                    saveToLocalDatabase(updatedReservation)
                    
                    Toast.makeText(
                        this@FinalizeOperationActivity,
                        "Saved locally. Will sync when online.",
                        Toast.LENGTH_LONG
                    ).show()
                    
                    // Finish the activity
                    finish()
                }
            })
        } else {
            Toast.makeText(this, "Invalid reservation data", Toast.LENGTH_SHORT).show()
            resetFinalizeButton()
        }
    }

    /**
     * Reset the finalize button to its original state
     */
    private fun resetFinalizeButton() {
        btnFinalizeOperation.isEnabled = true
        btnFinalizeOperation.text = "Finalize Operation"
    }

    /**
     * Save completed reservation to local database
     * @param reservation The reservation with "Completed" status
     */
    private fun saveToLocalDatabase(reservation: Reservation) {
        try {
            val currentDateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            
            val saved = dbHelper.addReservation(
                reservationCode = reservation.reservationCode,
                fullName = reservation.fullName,
                nic = reservation.nic ?: "",
                vehicleNumber = reservation.vehicleNumber,
                stationId = reservation.stationId,
                stationName = reservation.stationName,
                slotNo = reservation.slotNo,
                bookingDate = reservation.reservationDate,
                reservationDate = reservation.reservationDate,
                startTime = reservation.startTime,
                endTime = reservation.endTime,
                status = "Completed",
                qrCode = reservation.qrCode,
                createdAt = currentDateTime,
                updatedAt = currentDateTime
            )
            
            if (saved) {
                println("✅ Reservation saved to local database: ${reservation.reservationCode}")
            } else {
                // Try updating if insert failed (record might already exist)
                val updated = dbHelper.updateReservation(
                    reservationCode = reservation.reservationCode,
                    fullName = reservation.fullName,
                    nic = reservation.nic ?: "",
                    vehicleNumber = reservation.vehicleNumber,
                    stationId = reservation.stationId,
                    stationName = reservation.stationName,
                    slotNo = reservation.slotNo,
                    reservationDate = reservation.reservationDate,
                    startTime = reservation.startTime,
                    endTime = reservation.endTime,
                    status = "Completed",
                    updatedAt = currentDateTime
                )
                
                if (updated) {
                    println("✅ Reservation updated in local database: ${reservation.reservationCode}")
                } else {
                    println("❌ Failed to save reservation to local database")
                }
            }
        } catch (e: Exception) {
            println("❌ Error saving to database: ${e.message}")
            e.printStackTrace()
        }
    }
}

