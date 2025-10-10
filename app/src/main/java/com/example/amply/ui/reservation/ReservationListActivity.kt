package com.example.amply.ui.reservation

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.amply.R
import com.example.amply.data.ReservationDatabaseHelper
import com.example.amply.model.Reservation
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import com.example.amply.model.ReservationExtended

class ReservationListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReservationViewAdapter
    private lateinit var dbHelper: ReservationDatabaseHelper

    // Retrofit API Interface
    interface ReservationApi {
        @GET("api/v1/reservations")
        fun getReservations(): Call<List<ReservationExtended>>
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_reservation_list)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.recyclerViewReservations)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize adapter with empty list and click listener
        adapter = ReservationViewAdapter(mutableListOf()) { reservation ->
            Toast.makeText(this, "Clicked: ${reservation.fullName}", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = adapter

        dbHelper = ReservationDatabaseHelper(this)

        fetchReservationsFromApi()
    }

    private fun fetchReservationsFromApi() {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder().addInterceptor(logging).build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://conor-truculent-rurally.ngrok-free.dev/") // Replace with your API URL
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(ReservationApi::class.java)

        api.getReservations().enqueue(object : Callback<List<ReservationExtended>> {
            override fun onResponse(call: Call<List<ReservationExtended>>, response: Response<List<ReservationExtended>>) {
                if (response.isSuccessful) {
                    val reservations = response.body() ?: emptyList()
                    if (reservations.isNotEmpty()) {
                        adapter.updateData(reservations)

                        // Optional: Sync to local DB
                        syncToLocalDatabase(reservations)
                    } else {
                        Toast.makeText(this@ReservationListActivity, "No reservations found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@ReservationListActivity, "API Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<ReservationExtended>>, t: Throwable) {
                Toast.makeText(this@ReservationListActivity, "Failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun syncToLocalDatabase(reservations: List<ReservationExtended>) {
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
                r.status,
                r.qrCode,
                r.createdAt,
                r.updatedAt
            )
        }
    }
}
