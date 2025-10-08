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
import com.example.amply.ui.reservation.ReservationAdapter
import com.example.amply.data.ReservationDatabaseHelper
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

class ReservationListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReservationAdapter
    private lateinit var dbHelper: ReservationDatabaseHelper

    // Retrofit Data Model
    data class Reservation(
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
        @SerializedName("status") val status: String,
        @SerializedName("qrCode") val qrCode: String?,
        @SerializedName("createdAt") val createdAt: String,
        @SerializedName("updatedAt") val updatedAt: String
    )

    // Retrofit API Interface
    interface ReservationApi {
        @GET("api/v1/reservations")
        fun getReservations(): Call<List<Reservation>>
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
        adapter = ReservationAdapter(mutableListOf())
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

        api.getReservations().enqueue(object : Callback<List<Reservation>> {
            override fun onResponse(call: Call<List<Reservation>>, response: Response<List<Reservation>>) {
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

            override fun onFailure(call: Call<List<Reservation>>, t: Throwable) {
                Toast.makeText(this@ReservationListActivity, "Failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun syncToLocalDatabase(reservations: List<Reservation>) {
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