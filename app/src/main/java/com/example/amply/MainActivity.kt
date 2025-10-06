package com.example.amply

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.amply.R.*
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

class MainActivity : AppCompatActivity() {

    // 1️⃣ Define your API response model
    data class Reservation(
        @SerializedName("id") val id: Int,
        @SerializedName("name") val name: String,
        @SerializedName("date") val date: String
    )

    // 2️⃣ Retrofit interface
    interface ReservationApi {
        @GET("api/v1/reservations")
        fun getReservations(): Call<List<Reservation>>
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val textView: TextView = findViewById(id.textView) // make sure to set android:id="@+id/textView"

        // 3️⃣ Setup Retrofit with logging
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient.Builder().addInterceptor(logging).build()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://172.20.10.4:5016/") // Android emulator alias to localhost + fixed port
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(ReservationApi::class.java)

        // 4️⃣ Call the API
        api.getReservations().enqueue(object : Callback<List<Reservation>> {
            override fun onResponse(
                call: Call<List<Reservation>>,
                response: Response<List<Reservation>>
            ) {
                if (response.isSuccessful) {
                    val reservations = response.body()
                    textView.text = reservations?.joinToString("\n") {
                        "${it.id}: ${it.name} - ${it.date}"
                    } ?: "No reservations found"
                } else {
                    textView.text = "Error: ${response.code()}"
                }
            }

            override fun onFailure(call: Call<List<Reservation>>, t: Throwable) {
                textView.text = "Failed: ${t.message}"
            }
        })
    }
}
