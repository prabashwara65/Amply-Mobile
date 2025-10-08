//package com.example.amply
//
//import android.content.Intent
//import android.os.Bundle
//import android.widget.Button
//import android.widget.EditText
//import android.widget.Toast
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//import com.google.gson.annotations.SerializedName
//import okhttp3.OkHttpClient
//import okhttp3.logging.HttpLoggingInterceptor
//import retrofit2.*
//import retrofit2.converter.gson.GsonConverterFactory
//import retrofit2.http.GET
//
//class LoginActivity : AppCompatActivity() {
//
//    private lateinit var dbHelper: DatabaseHelper
//
//    // Retrofit Data Model
//    data class UserProfile(
//        @SerializedName("nic") val nic: String,
//        @SerializedName("fullName") val fullName: String,
//        @SerializedName("email") val email: String,
//        @SerializedName("phone") val phone: String,
//        @SerializedName("password") val password: String,
//        @SerializedName("createdAt") val createdAt: String,
//        @SerializedName("updatedAt") val updatedAt: String
//    )
//
//    // Retrofit API Interface
//    interface UserProfileApi {
//        @GET("api/v1/userprofiles")
//        fun getUserProfiles(): Call<List<UserProfile>>
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_login)
//
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//
//        dbHelper = DatabaseHelper(this)
//
//        val etEmail = findViewById<EditText>(R.id.username) // email field
//        val etPassword = findViewById<EditText>(R.id.password) // password field
//        val btnLogin = findViewById<Button>(R.id.loginBtn)
//
//        btnLogin.setOnClickListener {
//            val email = etEmail.text.toString().trim()
//            val password = etPassword.text.toString().trim()
//
//            if (email.isEmpty() || password.isEmpty()) {
//                Toast.makeText(this, "Enter both email and password", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            // Check SQLite first
//            if (dbHelper.validateUser(email, password)) {
//                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
//                startActivity(Intent(this, MainActivity::class.java))
//                finish()
//            } else {
//                // Fetch from API and sync
//                fetchUsersFromApiAndLogin(email, password)
//            }
//        }
//    }
//
//    private fun fetchUsersFromApiAndLogin(email: String, password: String) {
//        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
//        val client = OkHttpClient.Builder().addInterceptor(logging).build()
//
//        val retrofit = Retrofit.Builder()
//            .baseUrl("https://conor-truculent-rurally.ngrok-free.dev/") // replace with your API URL
//            .client(client)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//
//        val api = retrofit.create(UserProfileApi::class.java)
//        api.getUserProfiles().enqueue(object : Callback<List<UserProfile>> {
//            override fun onResponse(call: Call<List<UserProfile>>, response: Response<List<UserProfile>>) {
//                if (response.isSuccessful) {
//                    val users = response.body() ?: emptyList()
//                    val matchedUser = users.find { it.email == email && it.password == password }
//
//                    if (matchedUser != null) {
//                        // Save to SQLite if not exists
//                        if (!dbHelper.checkUser(email)) {
//                            dbHelper.addUser(email, password)
//                        }
//                        Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT).show()
//                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
//                        finish()
//                    } else {
//                        Toast.makeText(this@LoginActivity, "Invalid email or password", Toast.LENGTH_SHORT).show()
//                    }
//                } else {
//                    Toast.makeText(this@LoginActivity, "API Error: ${response.code()}", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            override fun onFailure(call: Call<List<UserProfile>>, t: Throwable) {
//                Toast.makeText(this@LoginActivity, "Failed to fetch users: ${t.message}", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }
//}


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
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    // -------------------- Retrofit Models --------------------
    data class UserProfile(
        val nic: String,
        val fullName: String,
        val email: String,
        val password: String,
        val phone: String,
        val role: String?,
        val createdAt: String,
        val updatedAt: String
    )

    // -------------------- Retrofit APIs --------------------
    interface UserProfileApi {
        @GET("api/v1/userprofiles")
        fun getUserProfiles(): Call<List<UserProfile>>
    }

    // -------------------- onCreate --------------------
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

        val etEmail = findViewById<EditText>(R.id.username)
        val etPassword = findViewById<EditText>(R.id.password)
        val btnLogin = findViewById<Button>(R.id.loginBtn)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Enter both email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // First check local cache
            if (dbHelper.validateUser(email, password)) {
                Toast.makeText(this, "Login successful (local cache)", Toast.LENGTH_SHORT).show()
                navigateBasedOnRole(dbHelper.getUserRole(email))
            } else {
                // Otherwise, fetch all users and compare
                fetchUsersAndLogin(email, password)
            }
        }
    }

    // -------------------- Fetch All Users and Compare --------------------
    private fun fetchUsersAndLogin(email: String, password: String) {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://conor-truculent-rurally.ngrok-free.dev/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val userApi = retrofit.create(UserProfileApi::class.java)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = userApi.getUserProfiles().execute()

                withContext(Dispatchers.Main) {
                    if (!response.isSuccessful) {
                        Toast.makeText(
                            this@LoginActivity,
                            "Failed to fetch users: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@withContext
                    }

                    val users = response.body() ?: emptyList()
                    val matchedUser = users.find {
                        it.email.equals(email, ignoreCase = true) &&
                                it.password == password
                    }



                    if (matchedUser != null) {
                        // Save to SQLite if not exists
                        if (!dbHelper.checkUser(email)) {
                            dbHelper.addUser(email, password, matchedUser.role ?: "")
                        }

                        Toast.makeText(
                            this@LoginActivity,
                            "Welcome ${matchedUser.fullName}",
                            Toast.LENGTH_SHORT
                        ).show()

                        navigateBasedOnRole(matchedUser.role)
                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            "Invalid email or password",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            } catch (e: SocketTimeoutException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity, "Server timeout. Try again later.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // -------------------- Navigate Based on Role --------------------
    private fun navigateBasedOnRole(role: String?) {
        when (role) {
            "Backofficer" -> startActivity(Intent(this, BackOfficeDashboard::class.java))
            "EvOperator", "ElectiveOperator" -> startActivity(Intent(this, EvOperatorDashboard::class.java))
            "EvOwner" -> startActivity(Intent(this, EvOwnerDashboard::class.java))
            else -> Toast.makeText(this, "Unknown role", Toast.LENGTH_SHORT).show()
        }
        finish()
    }
}
