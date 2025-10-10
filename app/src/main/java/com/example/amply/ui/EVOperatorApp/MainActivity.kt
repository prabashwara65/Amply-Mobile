package com.example.amply.ui.EVOperatorApp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.amply.R

/**
 * MainActivity for EV Operator Module
 * This is the main entry point for the EV Operator functionality
 * Provides navigation to QR Scanner, Booking Details, and Finalize Operation screens
 */
class MainActivity : AppCompatActivity() {

    // UI Components
    private lateinit var btnScanQR: Button
    private lateinit var btnViewBooking: Button
    private lateinit var btnFinalizeOperation: Button

    /**
     * onCreate method - initializes the activity and sets up UI components
     * @param savedInstanceState Bundle containing the activity's previously saved state
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ev_operator_main)

        // Initialize UI components
        initializeViews()

        // Set up button click listeners
        setupClickListeners()
    }

    /**
     * Initialize all UI views by finding them from the layout
     */
    private fun initializeViews() {
        btnScanQR = findViewById(R.id.btnScanQR)
        btnViewBooking = findViewById(R.id.btnViewBooking)
        btnFinalizeOperation = findViewById(R.id.btnFinalizeOperation)
    }

    /**
     * Set up click listeners for all buttons
     * Each button navigates to its respective activity
     */
    private fun setupClickListeners() {
        // Navigate to QR Scanner Activity
        btnScanQR.setOnClickListener {
            navigateToQRScanner()
        }

        // Navigate to Booking Details Activity
        btnViewBooking.setOnClickListener {
            navigateToBookingDetails()
        }

        // Navigate to Finalize Operation Activity
        btnFinalizeOperation.setOnClickListener {
            navigateToFinalizeOperation()
        }
    }

    /**
     * Navigate to QR Scanner Activity using Intent
     */
    private fun navigateToQRScanner() {
        val intent = Intent(this, QRScannerActivity::class.java)
        startActivity(intent)
    }

    /**
     * Navigate to Booking Details Activity using Intent
     */
    private fun navigateToBookingDetails() {
        val intent = Intent(this, BookingDetailsActivity::class.java)
        startActivity(intent)
    }

    /**
     * Navigate to Finalize Operation Activity using Intent
     */
    private fun navigateToFinalizeOperation() {
        val intent = Intent(this, FinalizeOperationActivity::class.java)
        startActivity(intent)
    }
}

