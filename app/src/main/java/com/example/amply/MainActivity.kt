package com.example.amply

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.amply.data.AuthDatabaseHelper
import com.example.amply.databinding.ActivityMainBinding
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import com.google.gson.annotations.SerializedName

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var dbHelper: AuthDatabaseHelper

    data class UserProfile(
        @SerializedName("nic") val nic: String,
        @SerializedName("fullName") val fullName: String,
        @SerializedName("email") val email: String,
        @SerializedName("phone") val phone: String,
        @SerializedName("password") val password: String,
        @SerializedName("createdAt") val createdAt: String,
        @SerializedName("updatedAt") val updatedAt: String
    )

    interface UserProfileApi {
        @GET("api/v1/userprofiles")
        fun getUserProfiles(): Call<List<UserProfile>>
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = AuthDatabaseHelper(this)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        fetchAndSaveUsers()
    }

    private fun fetchAndSaveUsers() {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder().addInterceptor(logging).build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://conor-truculent-rurally.ngrok-free.dev/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(UserProfileApi::class.java)

        api.getUserProfiles().enqueue(object : Callback<List<UserProfile>> {
            override fun onResponse(
                call: Call<List<UserProfile>>,
                response: Response<List<UserProfile>>
            ) {
                if (response.isSuccessful) {
                    val users = response.body()
                    if (!users.isNullOrEmpty()) {
                        dbHelper.clearUsers()
                        users.forEach { user ->
                            dbHelper.addUser(user.email, user.password)
                        }
                        binding.textView.text = "✅ ${users.size} users synced to local database."
                    } else {
                        binding.textView.text = "No user profiles found."
                    }
                } else {
                    binding.textView.text = "Error: ${response.code()}"
                }
            }

            override fun onFailure(call: Call<List<UserProfile>>, t: Throwable) {
                binding.textView.text = "Failed: ${t.message}"
            }
        })
    }
}
