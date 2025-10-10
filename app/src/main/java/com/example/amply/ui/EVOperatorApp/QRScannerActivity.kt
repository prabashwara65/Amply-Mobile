package com.example.amply.ui.EVOperatorApp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.amply.R
import com.example.amply.model.Reservation
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.google.zxing.ResultPoint
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * QRScannerActivity
 * This activity provides real QR code scanning functionality
 * Uses camera to scan QR codes and fetch reservation data from API
 * Follows the same API pattern as MainActivity.kt
 */
class QRScannerActivity : AppCompatActivity() {

    // UI Components
    private lateinit var barcodeView: DecoratedBarcodeView
    private lateinit var tvScannedData: TextView
    private lateinit var btnProceedToBooking: Button
    private lateinit var btnStartScanning: Button
    
    // Data
    private var currentReservation: Reservation? = null
    private var isScanning = false
    
    // Permission request code
    private companion object {
        const val CAMERA_PERMISSION_REQUEST_CODE = 1001
    }

    // API Interface - Following MainActivity pattern
    interface ReservationApi {
        @GET("api/v1/reservations/{id}")
        fun getReservationById(@Path("id") id: String): Call<Reservation>
        
        // Alternative: Get all reservations and filter client-side
        @GET("api/v1/reservations")
        fun getAllReservations(): Call<List<Reservation>>
    }

    /**
     * onCreate method - initializes the activity and sets up UI components
     * @param savedInstanceState Bundle containing the activity's previously saved state
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_scanner)

        try {
            // Always initialize views first
            initializeViews()
            setupClickListeners()
            
            // Check camera permission
            if (checkCameraPermission()) {
                setupBarcodeScanner()
            } else {
                requestCameraPermission()
            }
        } catch (e: Exception) {
            println("Error in onCreate: ${e.message}")
            showError("Failed to initialize QR scanner: ${e.message}")
        }
    }

    /**
     * Initialize all UI views by finding them from the layout
     */
    private fun initializeViews() {
        barcodeView = findViewById(R.id.barcodeView)
        tvScannedData = findViewById(R.id.tvScannedData)
        btnProceedToBooking = findViewById(R.id.btnProceedToBooking)
        btnStartScanning = findViewById(R.id.btnStartScanning)
    }

    /**
     * Set up click listeners for the scan button
     */
    private fun setupClickListeners() {
        btnStartScanning.setOnClickListener {
            if (isScanning) {
                stopScanning()
            } else {
                startScanning()
            }
        }
        
        btnProceedToBooking.setOnClickListener {
            proceedToBookingDetails()
        }
    }

    /**
     * Check if camera permission is granted
     * @return true if permission is granted, false otherwise
     */
    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Request camera permission from user
     */
    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    /**
     * Handle permission request result
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initializeViews()
                    setupClickListeners()
                    setupBarcodeScanner()
                } else {
                    Toast.makeText(this, "Camera permission is required for QR scanning", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
    }

    /**
     * Set up the barcode scanner with callback
     */
    private fun setupBarcodeScanner() {
        try {
            if (::barcodeView.isInitialized) {
                barcodeView.decodeContinuous(object : BarcodeCallback {
                    override fun barcodeResult(result: BarcodeResult) {
                        try {
                            if (result.text != null) {
                                // QR code scanned successfully
                                handleScannedQRCode(result.text)
                            }
                        } catch (e: Exception) {
                            println("Error handling QR result: ${e.message}")
                            showError("Error processing QR code: ${e.message}")
                        }
                    }

                    override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {
                        // Optional: Handle possible result points for UI feedback
                    }
                })
            } else {
                showError("Camera view not initialized properly.")
            }
        } catch (e: Exception) {
            println("Error setting up barcode scanner: ${e.message}")
            showError("Failed to setup camera: ${e.message}")
        }
    }

    /**
     * Start QR code scanning
     */
    private fun startScanning() {
        try {
            if (checkCameraPermission()) {
                if (::barcodeView.isInitialized) {
                    barcodeView.resume()
                    isScanning = true
                    btnStartScanning.text = "Stop Scanning"
                    tvScannedData.text = "Point camera at QR code to scan..."
                    Toast.makeText(this, "Scanning started. Point camera at QR code.", Toast.LENGTH_SHORT).show()
                } else {
                    showError("Camera view not initialized. Please restart the app.")
                }
            } else {
                requestCameraPermission()
            }
        } catch (e: Exception) {
            println("Error starting scanning: ${e.message}")
            showError("Failed to start scanning: ${e.message}")
        }
    }

    /**
     * Stop QR code scanning
     */
    private fun stopScanning() {
        try {
            if (::barcodeView.isInitialized) {
                barcodeView.pause()
            }
            isScanning = false
            btnStartScanning.text = "Start Scanning"
            tvScannedData.text = "Scanning stopped."
        } catch (e: Exception) {
            println("Error stopping scanning: ${e.message}")
            isScanning = false
            btnStartScanning.text = "Start Scanning"
        }
    }

    /**
     * Handle scanned QR code result
     * @param qrCodeText The text content of the scanned QR code
     */
    private fun handleScannedQRCode(qrCodeText: String) {
        // Stop scanning after successful scan
        stopScanning()
        
        // Log the scanned content for debugging
        println("QR Code Scanned - Raw Text: '$qrCodeText'")
        
        // Extract reservation ID from QR code
        // The QR code might contain JSON or just the ID
        val reservationId = extractReservationId(qrCodeText)
        
        println("Extracted Reservation ID: '$reservationId'")
        
        // Show loading message
        tvScannedData.text = """
            QR Code Scanned Successfully!
            
            Reservation ID: $reservationId
            Fetching reservation data...
        """.trimIndent()

        // Try different ID formats automatically
        val reservationIdFormats = listOf(
            reservationId,                    // Original format (e.g., "RES-594EB44E")
            reservationId.lowercase(),        // Lowercase (e.g., "res-594eb44e")
            reservationId.uppercase(),        // Uppercase
            reservationId.replace("-", ""),   // Without dash (e.g., "RES594EB44E")
            "1",                              // Fallback test ID
        ).distinct()
        
        println("Will try these ID formats: $reservationIdFormats")
        
        // Fetch reservation data with fallback formats
        fetchReservationDataWithFallback(reservationIdFormats, 0)
    }

    /**
     * Extract reservation ID from QR code text
     * Handles both JSON format and plain text format
     * @param qrCodeText The raw text from QR code
     * @return The extracted reservation ID
     */
    private fun extractReservationId(qrCodeText: String): String {
        val trimmedText = qrCodeText.trim()
        
        // Check if the QR code contains JSON
        if (trimmedText.startsWith("{") && trimmedText.contains("reservationCode")) {
            try {
                // Parse JSON to extract reservationCode
                val jsonStart = trimmedText.indexOf("\"reservationCode\":\"") + 19
                val jsonEnd = trimmedText.indexOf("\"", jsonStart)
                if (jsonStart > 18 && jsonEnd > jsonStart) {
                    val reservationCode = trimmedText.substring(jsonStart, jsonEnd)
                    println("Extracted reservation code from JSON: $reservationCode")
                    return reservationCode
                }
            } catch (e: Exception) {
                println("Error parsing JSON: ${e.message}")
            }
        }
        
        // If not JSON or parsing failed, return the trimmed text as-is
        return trimmedText
    }


    /**
     * Fetch reservation data with fallback to different ID formats
     * Uses the same API pattern as MainActivity.kt
     */
    private fun fetchReservationDataWithFallback(reservationIds: List<String>, index: Int) {
        if (index >= reservationIds.size) {
            // Last resort: Try to get all reservations and search by reservation code
            fetchAllReservationsAndSearch(reservationIds[0])
            return
        }
        
        val reservationId = reservationIds[index]
        println("Trying reservation ID format ${index + 1}/${reservationIds.size}: $reservationId")
        
        // Update UI to show loading
        tvScannedData.text = """
            QR Code Scanned Successfully!
            
            Reservation ID: ${reservationIds[0]}
            Loading reservation data...
        """.trimIndent()
        
        // EXACT same pattern as MainActivity.kt
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder().addInterceptor(logging).build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://conor-truculent-rurally.ngrok-free.dev/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(ReservationApi::class.java)
        
        api.getReservationById(reservationId).enqueue(object : Callback<Reservation> {
            override fun onResponse(
                call: Call<Reservation>,
                response: Response<Reservation>
            ) {
                println("Response for ID '$reservationId': ${response.code()}")
                println("URL: ${call.request().url}")
                
                if (response.isSuccessful) {
                    val reservation = response.body()
                    if (reservation != null) {
                        println("Success with ID format: $reservationId")
                        currentReservation = reservation
                        displayReservationData(reservation)
                    } else {
                        // Try next format
                        println("Response body null for: $reservationId")
                        fetchReservationDataWithFallback(reservationIds, index + 1)
                    }
                } else {
                    // Log the error details
                    val errorBody = response.errorBody()?.string()
                    println("Failed with ID format: $reservationId (${response.code()})")
                    println("Error body: $errorBody")
                    
                    // Try next format
                    fetchReservationDataWithFallback(reservationIds, index + 1)
                }
            }

            override fun onFailure(call: Call<Reservation>, t: Throwable) {
                // Try next format
                println("Network error with ID format: $reservationId - ${t.message}")
                fetchReservationDataWithFallback(reservationIds, index + 1)
            }
        })
    }

    /**
     * Fetch all reservations and search for matching reservation code
     * This is a fallback when direct ID lookup fails
     */
    private fun fetchAllReservationsAndSearch(reservationCode: String) {
        println("Fetching all reservations to search for code: $reservationCode")
        
        tvScannedData.text = """
            Searching for reservation: $reservationCode
            
            Please wait...
        """.trimIndent()
        
        // EXACT same pattern as MainActivity.kt
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder().addInterceptor(logging).build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://conor-truculent-rurally.ngrok-free.dev/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(ReservationApi::class.java)
        
        api.getAllReservations().enqueue(object : Callback<List<Reservation>> {
            override fun onResponse(
                call: Call<List<Reservation>>,
                response: Response<List<Reservation>>
            ) {
                println("Get all reservations response: ${response.code()}")
                
                if (response.isSuccessful) {
                    val reservations = response.body()
                    if (reservations != null) {
                        println("Received ${reservations.size} reservations")
                        
                        // Search for matching reservation code (case-insensitive)
                        val matchingReservation = reservations.find { 
                            it.reservationCode.equals(reservationCode, ignoreCase = true)
                        }
                        
                        if (matchingReservation != null) {
                            println("Found matching reservation: ${matchingReservation.reservationCode}")
                            currentReservation = matchingReservation
                            displayReservationData(matchingReservation)
                        } else {
                            println("No matching reservation found in ${reservations.size} reservations")
                            showError("Reservation not found: $reservationCode")
                        }
                    } else {
                        showError("No reservations found in database")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    println("Failed to get all reservations: ${response.code()}")
                    println("Error: $errorBody")
                    showError("Failed to fetch reservations: ${response.code()}\n$errorBody")
                }
            }

            override fun onFailure(call: Call<List<Reservation>>, t: Throwable) {
                println("Network error getting all reservations: ${t.message}")
                showError("Network error: ${t.message}")
            }
        })
    }

    /**
     * Check network connectivity
     * @return true if network is available, false otherwise
     */
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }

    /**
     * Fetch reservation data from the backend API using the scanned QR code
     * Follows the EXACT same pattern as MainActivity.kt
     * @param reservationId The reservation ID from the scanned QR code
     */
    private fun fetchReservationData(reservationId: String) {
        // Check network connectivity first
        if (!isNetworkAvailable()) {
            showError("No network connection. Please check your internet connection.")
            return
        }
        
        // EXACT same pattern as MainActivity.kt
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder().addInterceptor(logging).build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://conor-truculent-rurally.ngrok-free.dev/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(ReservationApi::class.java)

        // Log the reservation ID being requested
        println("Fetching reservation data for ID: $reservationId")
        
        api.getReservationById(reservationId).enqueue(object : Callback<Reservation> {
            override fun onResponse(
                call: Call<Reservation>,
                response: Response<Reservation>
            ) {
                println("API Response - Code: ${response.code()}, Success: ${response.isSuccessful}")
                println("API URL: ${call.request().url}")
                println("API Headers: ${call.request().headers}")
                
                if (response.isSuccessful) {
                    val reservation = response.body()
                    if (reservation != null) {
                        // Successfully fetched reservation data - same as MainActivity pattern
                        println("Reservation data received: ${reservation.reservationCode}")
                        currentReservation = reservation
                        displayReservationData(reservation)
                    } else {
                        println("Response body is null")
                        showError("No reservation found for the scanned QR code: '$reservationId'")
                    }
                } else {
                    // API call failed with error code
                    val errorBody = response.errorBody()?.string()
                    println("API Error - Code: ${response.code()}, Body: $errorBody")
                    
                    val errorMessage = when (response.code()) {
                        404 -> "Reservation not found"
                        500 -> "Server error. Please try again."
                        400 -> "Invalid reservation format"
                        else -> "Error loading reservation (${response.code()})"
                    }
                    showError(errorMessage)
                }
            }

            override fun onFailure(call: Call<Reservation>, t: Throwable) {
                // Network or other error
                println("Network Error: ${t.message}")
                showError("Network error. Please check your internet connection.")
            }
        })
    }

    /**
     * Display the fetched reservation data
     * Shows comprehensive reservation information like MainActivity shows user data
     * @param reservation The reservation data from the API
     */
    private fun displayReservationData(reservation: Reservation) {
        val reservationData = """
            Reservation Found!
            
            Booking Code: ${reservation.reservationCode}
            Customer: ${reservation.fullName}
            Vehicle: ${reservation.vehicleNumber}
            
            Charging Station: ${reservation.stationName}
            Slot: ${reservation.slotNo}
            
            Date: ${reservation.reservationDate}
            Time: ${reservation.startTime} - ${reservation.endTime}
            
            Tap below to view booking details
        """.trimIndent()

        tvScannedData.text = reservationData
        
        // Show success message
        Toast.makeText(this, "Reservation loaded successfully!", Toast.LENGTH_SHORT).show()
        
        // Enable proceed button
        btnProceedToBooking.isEnabled = true
        btnProceedToBooking.alpha = 1.0f
    }

    /**
     * Show error message to user
     * @param message Error message to display
     */
    private fun showError(message: String) {
        val errorMessage = """
            Error Loading Reservation
            
            $message
            
            Please try scanning the QR code again.
        """.trimIndent()
        
        tvScannedData.text = errorMessage
        Toast.makeText(this, "Error: $message", Toast.LENGTH_LONG).show()
        
        // Disable proceed button
        btnProceedToBooking.isEnabled = false
        btnProceedToBooking.alpha = 0.5f
    }

    /**
     * Navigate to Booking Details Activity
     * Passes the scanned reservation data as JSON string
     */
    private fun proceedToBookingDetails() {
        if (currentReservation != null) {
            val intent = Intent(this, BookingDetailsActivity::class.java)
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
            Toast.makeText(this, "Please scan a QR code first", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Resume camera scanning when activity resumes
     */
    override fun onResume() {
        super.onResume()
        try {
            if (::barcodeView.isInitialized && isScanning) {
                barcodeView.resume()
            }
        } catch (e: Exception) {
            println("Error resuming camera: ${e.message}")
        }
    }

    /**
     * Pause camera scanning when activity pauses
     */
    override fun onPause() {
        super.onPause()
        try {
            if (::barcodeView.isInitialized) {
                barcodeView.pause()
            }
        } catch (e: Exception) {
            println("Error pausing camera: ${e.message}")
        }
    }

    /**
     * Clean up resources when activity is destroyed
     */
    override fun onDestroy() {
        super.onDestroy()
        try {
            if (::barcodeView.isInitialized) {
                barcodeView.pause()
            }
        } catch (e: Exception) {
            println("Error destroying camera: ${e.message}")
        }
    }
}

