package com.example.amply.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.amply.R
import com.example.amply.network.RetrofitClient
import com.example.amply.network.UserProfileApi
import com.example.amply.model.OwnerProfile
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.gson.JsonParser

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etEmail = findViewById<EditText>(R.id.username)
        val etPassword = findViewById<EditText>(R.id.password)
        val etConfirmPassword = findViewById<EditText>(R.id.confirmPassword)
        val etFullName = findViewById<EditText>(R.id.fullName)
        val etNic = findViewById<EditText>(R.id.nic)
        val etPhone = findViewById<EditText>(R.id.phone)
        val btnRegister = findViewById<Button>(R.id.registerBtn)

        val userProfileApi = RetrofitClient.instance.create(UserProfileApi::class.java)

        btnRegister.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()
            val fullName = etFullName.text.toString().trim()
            val nic = etNic.text.toString().trim()
            val phone = etPhone.text.toString().trim()

            // Client-side validation
            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() ||
                fullName.isEmpty() || nic.isEmpty() || phone.isEmpty()
            ) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = OwnerProfile(
                nic = nic,
                fullName = fullName,
                email = email,
                password = password,
                phone = phone,
            )

            // API call
            userProfileApi.createUserProfile(user).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@RegisterActivity, "Registration successful", Toast.LENGTH_SHORT).show()

                        // Navigate to LoginActivity
                        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish() // Finish RegisterActivity so user can't go back
                    } else {
                        // Parse error body for validation messages
                        val errorBody = response.errorBody()?.string()
                        var errorMessage = "Unknown error"

                        errorBody?.let {
                            try {
                                val json = JsonParser.parseString(it).asJsonObject
                                if (json.has("message")) {
                                    errorMessage = json.get("message").asString
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                        Toast.makeText(this@RegisterActivity, "Validation Error: $errorMessage", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@RegisterActivity, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }
    }
}
