package com.example.amply

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

class LoginActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    // Retrofit Data Model
    data class UserProfile(
        @SerializedName("nic") val nic: String,
        @SerializedName("fullName") val fullName: String,
        @SerializedName("email") val email: String,
        @SerializedName("phone") val phone: String,
        @SerializedName("createdAt") val createdAt: String,
        @SerializedName("updatedAt") val updatedAt: String
    )

    // Retrofit API Interface
    interface UserProfileApi {
        @GET("api/v1/userprofiles")
        fun getUserProfiles(): Call<List<UserProfile>>
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dbHelper = DatabaseHelper(this)

        val etEmail = findViewById<EditText>(R.id.username) // email field
        val etPhone = findViewById<EditText>(R.id.password) // phone field
        val btnLogin = findViewById<Button>(R.id.loginBtn)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()

            if (email.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Enter both email and phone", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check SQLite first
            if (dbHelper.validateUser(email, phone)) {
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                // Fetch from API and sync
                fetchUsersFromApiAndLogin(email, phone)
            }
        }
    }

    private fun fetchUsersFromApiAndLogin(email: String, phone: String) {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder().addInterceptor(logging).build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://conor-truculent-rurally.ngrok-free.dev/") // replace with your API URL
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(UserProfileApi::class.java)
        api.getUserProfiles().enqueue(object : Callback<List<UserProfile>> {
            override fun onResponse(call: Call<List<UserProfile>>, response: Response<List<UserProfile>>) {
                if (response.isSuccessful) {
                    val users = response.body() ?: emptyList()
                    val matchedUser = users.find { it.email == email && it.phone == phone }

                    if (matchedUser != null) {
                        // Save to SQLite if not exists
                        if (!dbHelper.checkUser(email)) {
                            dbHelper.addUser(email, phone)
                        }
                        Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, "Invalid email or phone", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "API Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<UserProfile>>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "Failed to fetch users: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
