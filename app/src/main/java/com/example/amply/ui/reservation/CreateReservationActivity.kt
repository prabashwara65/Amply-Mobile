package com.example.amply.ui.reservation

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.amply.R
import com.example.amply.data.ReservationDatabaseHelper
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CreateReservationActivity : AppCompatActivity() {

    private lateinit var dbHelper: ReservationDatabaseHelper

    // --- Data Class for API ---
    data class ReservationCreateRequest(
        val NIC: String?,
        val FullName: String,
        val VehicleNumber: String,
        val StationId: String,
        val StationName: String,
        val SlotNo: Int,
        val ReservationDate: String,
        val StartTime: String,
        val EndTime: String
    )

    interface ReservationApi {
        @POST("api/v1/reservations")
        fun createReservation(@Body reservation: ReservationCreateRequest): Call<Void>
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_reservation)

        dbHelper = ReservationDatabaseHelper(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.createReservation)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val tvNIC = findViewById<EditText>(R.id.tvNIC)
        val tvFullName = findViewById<EditText>(R.id.tvFullName)
        val tvVehicleNumber = findViewById<EditText>(R.id.tvVehicleNumber)
        val tvStationName = findViewById<EditText>(R.id.tvStationName)
        val tvStationId = findViewById<EditText>(R.id.tvStationId)
        val tvReservationDate = findViewById<EditText>(R.id.tvReservationDate)
        val tvSlotNo = findViewById<EditText>(R.id.tvSlotNo)
        val tvStartTime = findViewById<EditText>(R.id.tvStartTime)
        val tvEndTime = findViewById<EditText>(R.id.tvEndTime)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitReservation)

        // --- Date Picker ---
        tvReservationDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val today = calendar.timeInMillis
            val datePicker = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val formattedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                    tvReservationDate.setText(formattedDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.datePicker.minDate = today
            calendar.add(Calendar.DAY_OF_MONTH, 7)
            datePicker.datePicker.maxDate = calendar.timeInMillis
            datePicker.show()
        }

        // --- Time Picker ---
        fun showTimePicker(editText: EditText) {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val timePicker = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                val displayHour: Int
                val amPm: String
                when {
                    selectedHour == 0 -> {
                        displayHour = 12; amPm = "AM"
                    }

                    selectedHour in 1..11 -> {
                        displayHour = selectedHour; amPm = "AM"
                    }

                    selectedHour == 12 -> {
                        displayHour = 12; amPm = "PM"
                    }

                    else -> {
                        displayHour = selectedHour - 12; amPm = "PM"
                    }
                }
                val formattedDisplay =
                    String.format("%02d:%02d %s", displayHour, selectedMinute, amPm)
                editText.setText(formattedDisplay)
            }, hour, minute, false)
            timePicker.show()
        }

        fun convertTo24Hour(timeStr: String): String {
            val parts = timeStr.split(" ")
            if (parts.size != 2) return "00:00:00"
            val hm = parts[0].split(":")
            var hour = hm[0].toIntOrNull() ?: 0
            val minute = hm[1].toIntOrNull() ?: 0
            val amPm = parts[1]
            if (amPm.equals("PM", true) && hour < 12) hour += 12
            if (amPm.equals("AM", true) && hour == 12) hour = 0
            return String.format("%02d:%02d:00", hour, minute)
        }

        tvStartTime.setOnClickListener { showTimePicker(tvStartTime) }
        tvEndTime.setOnClickListener { showTimePicker(tvEndTime) }

        // --- Submit Button ---
        btnSubmit.setOnClickListener {
            val reservation = ReservationCreateRequest(
                NIC = tvNIC.text.toString(),
                FullName = tvFullName.text.toString(),
                VehicleNumber = tvVehicleNumber.text.toString(),
                StationName = tvStationName.text.toString(),
                StationId = tvStationId.text.toString(),
                ReservationDate = tvReservationDate.text.toString(),
                SlotNo = tvSlotNo.text.toString().toIntOrNull() ?: 0,
                StartTime = convertTo24Hour(tvStartTime.text.toString()),
                EndTime = convertTo24Hour(tvEndTime.text.toString())
            )

            if (isOnline(this)) {
                createReservation(reservation)
            } else {
                saveOffline(reservation)
                Toast.makeText(this, "You are now offline. Reservation saved locally.", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    // ✅ Function to check internet connectivity
    private fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // ✅ Save reservation locally when offline
    private fun saveOffline(reservation: ReservationCreateRequest) {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val now = sdf.format(Date())
        val code = "OFFLINE-${System.currentTimeMillis()}"

        dbHelper.addReservation(
            reservationCode = code,
            fullName = reservation.FullName,
            nic = reservation.NIC ?: "",
            vehicleNumber = reservation.VehicleNumber,
            stationId = reservation.StationId,
            stationName = reservation.StationName,
            slotNo = reservation.SlotNo,
            bookingDate = now,
            reservationDate = reservation.ReservationDate,
            startTime = reservation.StartTime,
            endTime = reservation.EndTime,
            status = "Pending Sync",
            qrCode = null,
            createdAt = now,
            updatedAt = now
        )
    }

    // ✅ Retrofit POST API call
    private fun createReservation(reservation: ReservationCreateRequest) {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder().addInterceptor(logging).build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://conor-truculent-rurally.ngrok-free.dev/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(ReservationApi::class.java)
        api.createReservation(reservation).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@CreateReservationActivity, "Reservation created successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@CreateReservationActivity, "API Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@CreateReservationActivity, "Network error, saving offline...", Toast.LENGTH_SHORT).show()
                saveOffline(reservation)
            }
        })
    }
}