package com.example.amply.ui.reservation

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.amply.R
import com.example.amply.data.ReservationDatabaseHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ReservationListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReservationViewAdapter
    private lateinit var dbHelper: ReservationDatabaseHelper
    private lateinit var btnPending: Button
    private lateinit var btnConfirmed: Button

    private var allReservations = mutableListOf<ReservationExtended>()

    // -----------------------------
    // Data classes
    // -----------------------------
    data class ReservationExtended(
        val id: String,
        val reservationCode: String,
        val fullName: String,
        val nic: String?,
        val vehicleNumber: String,
        val stationId: String,
        val stationName: String,
        val slotNo: Int,
        val bookingDate: String,
        val reservationDate: String,
        val startTime: String,
        val endTime: String,
        val status: String,
        val qrCode: String?,
        val createdAt: String,
        val updatedAt: String
    )

    data class ApiReservation(
        @SerializedName("id") val id: String,
        @SerializedName("reservationCode") val reservationCode: String,
        @SerializedName("fullName") val fullName: String,
        @SerializedName("nic") val nic: String?,
        @SerializedName("vehicleNumber") val vehicleNumber: String,
        @SerializedName("stationId") val stationId: String,
        @SerializedName("stationName") val stationName: String,
        @SerializedName("slotNo") val slotNo: Int,
        @SerializedName("bookingDate") val bookingDate: String,
        @SerializedName("reservationDate") val reservationDate: String,
        @SerializedName("startTime") val startTime: String,
        @SerializedName("endTime") val endTime: String,
        @SerializedName("status") val status: String?,
        @SerializedName("qrCode") val qrCode: String?,
        @SerializedName("createdAt") val createdAt: String,
        @SerializedName("updatedAt") val updatedAt: String
    )

    interface ReservationApi {
        @GET("api/v1/reservations")
        fun getReservations(): Call<List<ApiReservation>>

        @DELETE("api/v1/reservations/{id}")
        fun deleteReservation(@Path("id") id: String): Call<Void>
    }

    // -----------------------------
    // onCreate()
    // -----------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_reservation_list)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dbHelper = ReservationDatabaseHelper(this)

        // Setup RecyclerView
        recyclerView = findViewById(R.id.recyclerViewReservations)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ReservationViewAdapter(mutableListOf(), { reservation ->
            Toast.makeText(this, "Clicked: ${reservation.stationName}", Toast.LENGTH_SHORT).show()
        }, { reservation ->
            updateReservation(reservation)
        }, { reservation ->
            deleteReservation(reservation)
        })
        recyclerView.adapter = adapter

        // Setup buttons
        btnPending = findViewById(R.id.btnPending)
        btnConfirmed = findViewById(R.id.btnConfirmed)

        btnPending.setOnClickListener {
            filterReservations("Pending")
            btnPending.setBackgroundColor(ContextCompat.getColor(this, R.color.black))
            btnConfirmed.setBackgroundColor(ContextCompat.getColor(this, R.color.gray))
        }

        btnConfirmed.setOnClickListener {
            filterReservations("Confirmed")
            btnConfirmed.setBackgroundColor(ContextCompat.getColor(this, R.color.black))
            btnPending.setBackgroundColor(ContextCompat.getColor(this, R.color.gray))
        }

        // FAB for adding reservation
        val fabAddReservation: FloatingActionButton = findViewById(R.id.fabAddReservation)
        fabAddReservation.setOnClickListener {
            val intent = Intent(this, CreateReservationActivity::class.java)
            startActivity(intent)
        }

        // Initial Fetch
        fetchReservationsFromApi()
    }

    //Auto-refresh list when coming back from CreateReservationActivity

    override fun onResume() {
        super.onResume()
        fetchReservationsFromApi()
    }



    // API Fetch

    private fun fetchReservationsFromApi() {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder().addInterceptor(logging).build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://conor-truculent-rurally.ngrok-free.dev/") // Replace with your API URL
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(ReservationApi::class.java)

        api.getReservations().enqueue(object : Callback<List<ApiReservation>> {
            override fun onResponse(call: Call<List<ApiReservation>>, response: Response<List<ApiReservation>>) {
                if (response.isSuccessful) {
                    val apiReservations = response.body() ?: emptyList()
                    if (apiReservations.isNotEmpty()) {
                        allReservations = apiReservations.map { r ->
                            ReservationExtended(
                                id = r.id,
                                reservationCode = r.reservationCode,
                                fullName = r.fullName,
                                nic = r.nic,
                                vehicleNumber = r.vehicleNumber,
                                stationId = r.stationId,
                                stationName = r.stationName,
                                slotNo = r.slotNo,
                                bookingDate = r.bookingDate,
                                reservationDate = r.reservationDate,
                                startTime = r.startTime,
                                endTime = r.endTime,
                                status = r.status ?: "Unknown",
                                qrCode = r.qrCode,
                                createdAt = r.createdAt,
                                updatedAt = r.updatedAt
                            )
                        }.toMutableList()

                        // Show Pending first
                        filterReservations("Pending")
                        btnPending.setBackgroundColor(ContextCompat.getColor(this@ReservationListActivity, R.color.black))
                        btnConfirmed.setBackgroundColor(ContextCompat.getColor(this@ReservationListActivity, R.color.gray))

                        // Sync to local DB
                        syncToLocalDatabase(apiReservations)
                    } else {
                        Toast.makeText(this@ReservationListActivity, "No reservations found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@ReservationListActivity, "API Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<ApiReservation>>, t: Throwable) {
                Toast.makeText(this@ReservationListActivity, "Failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // -----------------------------
    // Filtering and local sync
    // -----------------------------
    private fun filterReservations(status: String) {
        val filtered = allReservations.filter { it.status.equals(status, ignoreCase = true) }
        adapter.updateData(filtered)
    }

    private fun syncToLocalDatabase(reservations: List<ApiReservation>) {
        dbHelper.clearReservations()
        for (r in reservations) {
            dbHelper.addReservation(
                r.reservationCode,
                r.fullName,
                r.nic ?: "",
                r.vehicleNumber,
                r.stationId,
                r.stationName,
                r.slotNo,
                r.bookingDate,
                r.reservationDate,
                r.startTime,
                r.endTime,
                r.status ?: "",
                r.qrCode,
                r.createdAt,
                r.updatedAt
            )
        }
    }

    //update reservation
    private fun updateReservation(reservation: ReservationExtended) {
        try {
            // Combine reservation date and start time
            val rawDateTime = "${reservation.reservationDate} ${reservation.startTime}"

            // Adjust pattern to match your data format (example: "2025-10-08T00:00:00Z 07:30:00")
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z' HH:mm:ss", Locale.getDefault())
            val reservationDateTime = format.parse(rawDateTime)

            if (reservationDateTime == null) {
                Toast.makeText(this, "Invalid reservation date/time.", Toast.LENGTH_SHORT).show()
                return
            }

            // Calculate hours difference
            val diff = reservationDateTime.time - System.currentTimeMillis()
            val hours = diff / (1000 * 60 * 60)

            if (hours < 12) {
                Toast.makeText(
                    this,
                    "Reservation can only be updated at least 12 hours before.",
                    Toast.LENGTH_LONG
                ).show()
                return
            }

            // Proceed to update
            val intent = Intent(this, CreateReservationActivity::class.java).apply {
                putExtra("isUpdate", true)
                putExtra("id", reservation.id)
                putExtra("nic", reservation.nic)
                putExtra("fullName", reservation.fullName)
                putExtra("vehicleNumber", reservation.vehicleNumber)
                putExtra("stationId", reservation.stationId)
                putExtra("stationName", reservation.stationName)
                putExtra("slotNo", reservation.slotNo)
                // Extract only the date part
                val onlyDate = reservation.reservationDate.split("T")[0]
                putExtra("reservationDate", onlyDate)

                putExtra("startTime", reservation.startTime)
                putExtra("endTime", reservation.endTime)
            }
            startActivity(intent)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error parsing date: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


    //delete reservation
    private fun deleteReservation(reservation: ReservationExtended) {
        try {
            val rawDateTime = "${reservation.reservationDate} ${reservation.startTime}"
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z' HH:mm:ss", Locale.getDefault())
            val reservationDateTime = format.parse(rawDateTime)

            if (reservationDateTime == null) {
                Toast.makeText(this, "Invalid reservation date/time.", Toast.LENGTH_SHORT).show()
                return
            }

            val diff = reservationDateTime.time - System.currentTimeMillis()
            val hours = diff / (1000 * 60 * 60)

            if (hours < 12) {
                Toast.makeText(this, "Reservations can only be deleted at least 12 hours before.", Toast.LENGTH_LONG).show()
                return
            }

            AlertDialog.Builder(this)
                .setTitle("Delete Reservation")
                .setMessage("Are you sure you want to delete this reservation?")
                .setPositiveButton("Yes") { _, _ ->
                    performDeleteApiCall(reservation.id)
                }
                .setNegativeButton("No", null)
                .show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error parsing date: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun performDeleteApiCall(reservationId: String) {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder().addInterceptor(logging).build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://conor-truculent-rurally.ngrok-free.dev/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(ReservationApi::class.java)

        api.deleteReservation(reservationId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ReservationListActivity, "Reservation deleted successfully.", Toast.LENGTH_SHORT).show()
                    fetchReservationsFromApi()
                } else {
                    Toast.makeText(this@ReservationListActivity, "Failed to delete: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@ReservationListActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


}